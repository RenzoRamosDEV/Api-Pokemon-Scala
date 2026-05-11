package com.pokeapp.infrastructure.http.client

import cats.effect.Concurrent
import cats.syntax.all.*
import com.pokeapp.config.PokeApiConfig
import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.Pokemon
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.model.{EvolutionChain, PokemonSpecies}
import com.pokeapp.infrastructure.http.codec.EvolutionCodec.given
import com.pokeapp.infrastructure.http.codec.PokemonCodec.given
import com.pokeapp.infrastructure.http.codec.PokemonSpeciesCodec.given
import io.circe.Decoder
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.client.Client
import org.http4s.{Status, Uri}

// Cliente HTTP fuertemente tipado para los endpoints conocidos de PokeAPI.
// Cada método devuelve el modelo de dominio correspondiente, ya deserializado.
// Para recursos sin modelo propio (passthrough genérico) usar GenericPokeApiClient.
class PokeApiClient[F[_]: Concurrent](
    httpClient: Client[F],
    config: PokeApiConfig
):

  private def baseUri: Uri = Uri.unsafeFromString(config.baseUrl)

  // Método base que hace GET a una URI y traduce los status HTTP a DomainError.
  // `Decoder[A]` es provisto implícitamente por los codecs importados arriba.
  private def get[A: Decoder](uri: Uri): F[Either[DomainError, A]] =
    httpClient
      .run(org.http4s.Request[F](uri = uri))
      .use: response =>
        response.status match
          case Status.Ok          => response.as[A].map(Right(_))
          case Status.NotFound    => DomainError.NotFound(s"Resource not found at $uri").asLeft[A].pure[F]
          case Status.TooManyRequests => DomainError.RateLimitExceeded.asLeft[A].pure[F]
          case s => DomainError.ExternalApiError(s"Unexpected status $s", s.code).asLeft[A].pure[F]

  // ── Pokémon ──────────────────────────────────────────────────────────────

  def getPokemon(idOrName: String): F[Either[DomainError, Pokemon]] =
    get[Pokemon](baseUri / "pokemon" / idOrName)

  def listPokemon(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    get[PaginatedResponse[NamedResource]](
      (baseUri / "pokemon").withQueryParam("limit", limit).withQueryParam("offset", offset)
    )

  // ── Especie ───────────────────────────────────────────────────────────────

  // El endpoint pokemon-species comparte el mismo ID/nombre que el endpoint pokemon.
  def getPokemonSpecies(idOrName: String): F[Either[DomainError, PokemonSpecies]] =
    get[PokemonSpecies](baseUri / "pokemon-species" / idOrName)

  // La URL de la cadena evolutiva viene dentro del objeto PokemonSpecies,
  // por eso se recibe la URL completa en lugar de un ID.
  def getEvolutionChainByUrl(url: String): F[Either[DomainError, EvolutionChain]] =
    get[EvolutionChain](Uri.unsafeFromString(url))

  // ── Recursos genéricos tipados ────────────────────────────────────────────

  // Usado por los repositorios HTTP de Ability, Berry, Move, etc.
  // El `Decoder[A]` debe estar en scope en el sitio de llamada (importado desde el codec correspondiente).
  def getResource[A: Decoder](resourcePath: String, idOrName: String): F[Either[DomainError, A]] =
    get[A](baseUri / resourcePath / idOrName)

  def listResource(
      resourcePath: String,
      limit: Int,
      offset: Int
  ): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    get[PaginatedResponse[NamedResource]](
      (baseUri / resourcePath).withQueryParam("limit", limit).withQueryParam("offset", offset)
    )
