package com.pokeapp.api.routes

import cats.effect.Concurrent
import com.pokeapp.api.dto.{ApiError, ApiResponse, ResponseCodec}
import com.pokeapp.api.dto.ResponseCodec.given
import com.pokeapp.domain.error.DomainError
import io.circe.Encoder
import io.circe.syntax.*
import org.http4s.Response
import org.http4s.circe.CirceEntityEncoder.given
import org.http4s.dsl.Http4sDsl

// Trait base compartido por todas las rutas tipadas.
// Centraliza el manejo de errores de dominio → respuestas HTTP y la validación de paginación,
// evitando duplicar el mismo pattern match en cada route class.
trait RouteSupport[F[_]: Concurrent]:
  protected val dsl: Http4sDsl[F] = Http4sDsl[F]
  import dsl.*

  // Traduce un Either[DomainError, A] al código HTTP correspondiente.
  // Todos los errores se envuelven en el formato estándar ApiErrorResponse.
  protected def handleTyped[A: Encoder](result: Either[DomainError, A]): F[Response[F]] =
    result match
      case Right(value)                             => Ok(ApiResponse.live(value).asJson)
      case Left(DomainError.NotFound(msg))          => NotFound(ApiError.notFound(msg).asJson)
      case Left(DomainError.RateLimitExceeded)      => TooManyRequests(ApiError.rateLimitExceeded.asJson)
      case Left(DomainError.ExternalApiError(m, _)) => BadGateway(ApiError.externalError(m).asJson)
      case Left(err)                                => InternalServerError(ApiError.internalError(err.toString).asJson)

  // Alias de handleTyped para listas paginadas; mantiene la separación semántica
  // por si en el futuro se quiere agregar lógica diferente para respuestas de lista.
  protected def handleList[A: Encoder](result: Either[DomainError, A]): F[Response[F]] =
    handleTyped(result)

  // Valida que los parámetros de paginación estén dentro de rangos aceptables.
  // Devuelve Some(badRequest) si hay violación, None si los parámetros son válidos.
  // El límite máximo de 100 evita que una sola request sobrecargue PokeAPI.
  protected def validatePagination(limit: Int, offset: Int): Option[F[Response[F]]] =
    (limit, offset) match
      case (l, _) if l < 1 || l > 100 => Some(BadRequest(ApiError.badRequest("limit must be between 1 and 100").asJson))
      case (_, o) if o < 0            => Some(BadRequest(ApiError.badRequest("offset must be >= 0").asJson))
      case _                          => None
