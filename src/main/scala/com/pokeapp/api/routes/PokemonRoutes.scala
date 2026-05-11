package com.pokeapp.api.routes

import cats.effect.Concurrent
import cats.syntax.all.*
import com.pokeapp.api.dto.{ApiError, ApiResponse, ResponseCodec}
import com.pokeapp.api.dto.DomainEncoders.given
import com.pokeapp.api.dto.ResponseCodec.given
import com.pokeapp.application.pokemon.{GetPokemonFullUseCase, GetPokemonUseCase, ListPokemonUseCase}
import com.pokeapp.domain.error.DomainError
import io.circe.syntax.*
import org.http4s.HttpRoutes
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl

// Rutas HTTP específicas para el recurso Pokémon.
// Se monta antes que GenericResourceRoutes para que /pokemon/{id}/full y /pokemon/{id}
// sean manejados aquí y no caigan al handler genérico de passthrough.
class PokemonRoutes[F[_]: Concurrent](
    getPokemon: GetPokemonUseCase[F],
    listPokemon: ListPokemonUseCase[F],
    getPokemonFull: GetPokemonFullUseCase[F]
):
  private val dsl = Http4sDsl[F]
  import dsl.*

  private object LimitParam  extends OptionalQueryParamDecoderMatcher[Int]("limit")
  private object OffsetParam extends OptionalQueryParamDecoderMatcher[Int]("offset")

  private val MaxLimit = 100
  private val MinLimit = 1

  val routes: HttpRoutes[F] = HttpRoutes.of[F]:

    // GET /api/v1/pokemon?limit={n}&offset={n}
    // Devuelve lista paginada de Pokémon (solo nombre y URL de cada uno)
    case GET -> Root / "api" / "v1" / "pokemon" :? LimitParam(limit) +& OffsetParam(offset) =>
      val l = limit.getOrElse(20)
      val o = offset.getOrElse(0)
      if l < MinLimit || l > MaxLimit then
        BadRequest(ApiError.badRequest(s"limit must be between $MinLimit and $MaxLimit").asJson)
      else if o < 0 then
        BadRequest(ApiError.badRequest("offset must be >= 0").asJson)
      else
        listPokemon.execute(l, o).flatMap:
          case Right(page) => Ok(ApiResponse.live(page).asJson)
          case Left(DomainError.RateLimitExceeded) =>
            TooManyRequests(ApiError.rateLimitExceeded.asJson)
          case Left(DomainError.ExternalApiError(msg, _)) =>
            BadGateway(ApiError.externalError(msg).asJson)
          case Left(err) =>
            InternalServerError(ApiError.internalError(err.toString).asJson)

    // GET /api/v1/pokemon/{id}/full
    // Respuesta enriquecida: combina pokemon + pokemon-species + evolution-chain en una sola llamada.
    // Debe aparecer ANTES del handler /pokemon/{id} o el path "full" sería interpretado como un ID.
    case GET -> Root / "api" / "v1" / "pokemon" / id / "full" =>
      getPokemonFull.execute(id).flatMap:
        case Right(full)                     => Ok(ApiResponse.live(full).asJson)
        case Left(DomainError.NotFound(msg)) => NotFound(ApiError.notFound(msg).asJson)
        case Left(DomainError.RateLimitExceeded) =>
          TooManyRequests(ApiError.rateLimitExceeded.asJson)
        case Left(DomainError.ExternalApiError(msg, _)) =>
          BadGateway(ApiError.externalError(msg).asJson)
        case Left(err) =>
          InternalServerError(ApiError.internalError(err.toString).asJson)

    // GET /api/v1/pokemon/{id} o /api/v1/pokemon/{name}
    // Acepta tanto ID numérico como nombre de texto (ej: 25 o "pikachu")
    case GET -> Root / "api" / "v1" / "pokemon" / id =>
      val fetch = id.toIntOption.map(getPokemon.execute).getOrElse(getPokemon.executeByName(id))
      fetch.flatMap:
        case Right(pokemon)                  => Ok(ApiResponse.live(pokemon).asJson)
        case Left(DomainError.NotFound(msg)) => NotFound(ApiError.notFound(msg).asJson)
        case Left(DomainError.RateLimitExceeded) =>
          TooManyRequests(ApiError.rateLimitExceeded.asJson)
        case Left(DomainError.ExternalApiError(msg, _)) =>
          BadGateway(ApiError.externalError(msg).asJson)
        case Left(err) =>
          InternalServerError(ApiError.internalError(err.toString).asJson)
