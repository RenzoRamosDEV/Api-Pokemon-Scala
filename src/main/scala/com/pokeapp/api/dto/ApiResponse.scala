package com.pokeapp.api.dto

import java.time.Instant

// Indica si los datos vienen de caché local o de PokeAPI en tiempo real
enum DataSource:
  case Cache, Live

// Metadata incluida en cada respuesta exitosa para que el cliente sepa el origen y timestamp
case class ApiMeta(source: DataSource, timestamp: Instant)

// Envelope estándar para todas las respuestas exitosas de la API.
// Todos los endpoints devuelven { "data": ..., "meta": { "source": ..., "timestamp": ... } }
case class ApiResponse[A](data: A, meta: ApiMeta)

// Estructura de un error individual dentro de la respuesta de error
case class ApiError(code: String, message: String, timestamp: Instant)

// Envelope estándar para todas las respuestas de error.
// Todos los errores devuelven { "error": { "code": ..., "message": ..., "timestamp": ... } }
case class ApiErrorResponse(error: ApiError)

object ApiResponse:
  def live[A](data: A): ApiResponse[A] =
    ApiResponse(data, ApiMeta(DataSource.Live, Instant.now()))

  def cached[A](data: A): ApiResponse[A] =
    ApiResponse(data, ApiMeta(DataSource.Cache, Instant.now()))

// Constructores de error con códigos de error estandarizados.
// Los códigos son strings en SCREAMING_SNAKE_CASE para que sean fácilmente parseables por el frontend.
object ApiError:
  def notFound(message: String): ApiErrorResponse =
    ApiErrorResponse(ApiError("NOT_FOUND", message, Instant.now()))

  def badRequest(message: String): ApiErrorResponse =
    ApiErrorResponse(ApiError("BAD_REQUEST", message, Instant.now()))

  def externalError(message: String): ApiErrorResponse =
    ApiErrorResponse(ApiError("EXTERNAL_API_ERROR", message, Instant.now()))

  def rateLimitExceeded: ApiErrorResponse =
    ApiErrorResponse(ApiError("RATE_LIMIT_EXCEEDED", "Too many requests", Instant.now()))

  def internalError(message: String): ApiErrorResponse =
    ApiErrorResponse(ApiError("INTERNAL_ERROR", message, Instant.now()))
