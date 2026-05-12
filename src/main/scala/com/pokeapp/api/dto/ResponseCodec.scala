package com.pokeapp.api.dto

import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import io.circe.{Encoder, Json}
import io.circe.syntax.*

// Encoders Circe para los tipos del envelope de la API (ApiResponse, ApiError, ApiMeta)
// y para los tipos compartidos del dominio (NamedResource, PaginatedResponse).
// Se usan en todas las rutas; deben estar en scope vía `import ResponseCodec.given`.
object ResponseCodec:

  // Serializa el origen de datos a su representación string para el cliente
  given Encoder[DataSource] = Encoder[String].contramap:
    case DataSource.Live => "live"

  given Encoder[ApiMeta] = meta =>
    Json.obj(
      "source"    -> meta.source.asJson,
      "timestamp" -> Json.fromString(meta.timestamp.toString)
    )

  // El wrapper `data`/`meta` se construye aquí manualmente para controlar el formato JSON exacto
  given [A: Encoder]: Encoder[ApiResponse[A]] = resp =>
    Json.obj(
      "data" -> resp.data.asJson,
      "meta" -> resp.meta.asJson
    )

  given Encoder[ApiError] = error =>
    Json.obj(
      "code"      -> Json.fromString(error.code),
      "message"   -> Json.fromString(error.message),
      "timestamp" -> Json.fromString(error.timestamp.toString)
    )

  given Encoder[ApiErrorResponse] = resp => Json.obj("error" -> resp.error.asJson)

  // NamedResource se codifica aquí (y no en DomainEncoders) porque es un tipo compartido
  // que también usan los propios encoders del envelope (PaginatedResponse)
  given Encoder[NamedResource] = r =>
    Json.obj("name" -> Json.fromString(r.name), "url" -> Json.fromString(r.url))

  given [A: Encoder]: Encoder[PaginatedResponse[A]] = page =>
    Json.obj(
      "count"    -> page.count.asJson,
      "next"     -> page.next.asJson,
      "previous" -> page.previous.asJson,
      "results"  -> page.results.asJson
    )
