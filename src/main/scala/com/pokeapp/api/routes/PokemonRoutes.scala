package com.pokeapp.api.routes

import cats.effect.Concurrent
import cats.syntax.all.*
import com.pokeapp.api.dto.DomainEncoders.given
import com.pokeapp.api.dto.ResponseCodec.given
import com.pokeapp.application.pokemon.{GetPokemonFullUseCase, GetPokemonUseCase, ListPokemonUseCase}
import org.http4s.HttpRoutes

// Rutas HTTP específicas para el recurso Pokémon.
// Se monta antes que GenericResourceRoutes para que /pokemon/{id}/full y /pokemon/{id}
// sean manejados aquí y no caigan al handler genérico de passthrough.
// Extiende RouteSupport para reutilizar handleTyped, handleList y validatePagination
// sin duplicar el mismo pattern match sobre DomainError en cada handler.
class PokemonRoutes[F[_]: Concurrent](
    getPokemon: GetPokemonUseCase[F],
    listPokemon: ListPokemonUseCase[F],
    getPokemonFull: GetPokemonFullUseCase[F]
) extends RouteSupport[F]:
  import dsl.*

  private object LimitParam  extends OptionalQueryParamDecoderMatcher[Int]("limit")
  private object OffsetParam extends OptionalQueryParamDecoderMatcher[Int]("offset")

  val routes: HttpRoutes[F] = HttpRoutes.of[F]:

    // GET /api/v1/pokemon?limit={n}&offset={n}
    // Devuelve lista paginada de Pokémon (solo nombre y URL de cada uno)
    case GET -> Root / "api" / "v1" / "pokemon" :? LimitParam(limit) +& OffsetParam(offset) =>
      val l = limit.getOrElse(20)
      val o = offset.getOrElse(0)
      validatePagination(l, o).getOrElse(listPokemon.execute(l, o).flatMap(handleList))

    // GET /api/v1/pokemon/{id}/full
    // Respuesta enriquecida: combina pokemon + pokemon-species + evolution-chain en una sola llamada.
    // Debe aparecer ANTES del handler /pokemon/{id} o el path "full" sería interpretado como un ID.
    case GET -> Root / "api" / "v1" / "pokemon" / id / "full" =>
      getPokemonFull.execute(id).flatMap(handleTyped)

    // GET /api/v1/pokemon/{id} o /api/v1/pokemon/{name}
    // Acepta tanto ID numérico como nombre de texto (ej: 25 o "pikachu")
    case GET -> Root / "api" / "v1" / "pokemon" / id =>
      id.toIntOption.fold(getPokemon.executeByName(id))(getPokemon.execute).flatMap(handleTyped)
