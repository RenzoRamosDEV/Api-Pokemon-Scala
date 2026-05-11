package com.pokeapp.api.routes

import cats.effect.Concurrent
import cats.syntax.all.*
import com.pokeapp.api.ResourceRegistry
import com.pokeapp.api.dto.{ApiError, ApiResponse, ResponseCodec}
import com.pokeapp.api.dto.ResponseCodec.given
import com.pokeapp.domain.error.DomainError
import com.pokeapp.infrastructure.http.client.GenericPokeApiClient
import io.circe.Json
import io.circe.syntax.*
import org.http4s.HttpRoutes
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl

// Rutas de passthrough genérico para los ~50 recursos de PokeAPI que no tienen
// modelo de dominio propio (egg-group, contest-type, language, version, etc.).
// El ResourceRegistry actúa como lista blanca: solo se aceptan recursos conocidos,
// evitando que esta ruta sirva de proxy abierto hacia cualquier path de PokeAPI.
class GenericResourceRoutes[F[_]: Concurrent](client: GenericPokeApiClient[F]):
  private val dsl = Http4sDsl[F]
  import dsl.*

  private object LimitParam  extends OptionalQueryParamDecoderMatcher[Int]("limit")
  private object OffsetParam extends OptionalQueryParamDecoderMatcher[Int]("offset")

  // Traduce DomainError → respuesta HTTP para el Json crudo
  private def handleResult(result: Either[DomainError, Json]) =
    result match
      case Right(json)                              => Ok(wrapLive(json))
      case Left(DomainError.NotFound(msg))          => NotFound(ApiError.notFound(msg).asJson)
      case Left(DomainError.RateLimitExceeded)      => TooManyRequests(ApiError.rateLimitExceeded.asJson)
      case Left(DomainError.ExternalApiError(m, _)) => BadGateway(ApiError.externalError(m).asJson)
      case Left(err)                                => InternalServerError(ApiError.internalError(err.toString).asJson)

  // Envuelve el Json crudo de PokeAPI en el envelope estándar { "data": ..., "meta": ... }
  private def wrapLive(data: Json): Json =
    ApiResponse.live(data).asJson

  val routes: HttpRoutes[F] = HttpRoutes.of[F]:

    // GET /api/v1/{resource}?limit={n}&offset={n}
    // El guard `if ResourceRegistry.all.contains(resource)` valida que el recurso sea conocido.
    // Si no está en el registry, http4s pasa al siguiente handler (que devolverá 404).
    case GET -> Root / "api" / "v1" / resource :? LimitParam(limit) +& OffsetParam(offset)
        if ResourceRegistry.all.contains(resource) =>
      val path = ResourceRegistry.all(resource)
      client.list(path, limit.getOrElse(20), offset.getOrElse(0)).flatMap(handleResult)

    // GET /api/v1/{resource}/{idOrName}
    case GET -> Root / "api" / "v1" / resource / idOrName
        if ResourceRegistry.all.contains(resource) =>
      val path = ResourceRegistry.all(resource)
      client.getByIdOrName(path, idOrName).flatMap(handleResult)
