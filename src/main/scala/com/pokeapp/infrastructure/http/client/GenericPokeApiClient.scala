package com.pokeapp.infrastructure.http.client

import cats.effect.Concurrent
import cats.syntax.all.*
import com.pokeapp.config.PokeApiConfig
import com.pokeapp.domain.error.DomainError
import io.circe.Json
import org.http4s.circe.*
import org.http4s.client.Client
import org.http4s.{Status, Uri}

// Cliente HTTP genérico que devuelve Json crudo sin deserializar a modelo de dominio.
// Se usa en GenericResourceRoutes para hacer passthrough de los ~50 recursos de PokeAPI
// que no tienen un modelo de dominio propio en esta aplicación (egg-group, contest-type, etc.).
// Para recursos tipados (Pokemon, Ability, etc.) usar PokeApiClient.
class GenericPokeApiClient[F[_]: Concurrent](
    httpClient: Client[F],
    config: PokeApiConfig
):
  private def baseUri: Uri = Uri.unsafeFromString(config.baseUrl)

  // Hace GET a la URI y devuelve el Json tal como lo entrega PokeAPI
  private def getJson(uri: Uri): F[Either[DomainError, Json]] =
    httpClient
      .run(org.http4s.Request[F](uri = uri))
      .use: response =>
        response.status match
          case Status.Ok          => response.as[Json].map(Right(_))
          case Status.NotFound    => DomainError.NotFound(s"Resource not found at $uri").asLeft[Json].pure[F]
          case Status.TooManyRequests => DomainError.RateLimitExceeded.asLeft[Json].pure[F]
          case s => DomainError.ExternalApiError(s"Unexpected status $s", s.code).asLeft[Json].pure[F]

  def getByIdOrName(resourcePath: String, idOrName: String): F[Either[DomainError, Json]] =
    getJson(baseUri / resourcePath / idOrName)

  def list(resourcePath: String, limit: Int, offset: Int): F[Either[DomainError, Json]] =
    getJson(
      (baseUri / resourcePath).withQueryParam("limit", limit).withQueryParam("offset", offset)
    )
