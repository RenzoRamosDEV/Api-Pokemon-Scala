package com.pokeapp.api.middleware

import cats.data.{Kleisli, OptionT}
import cats.effect.Concurrent
import cats.syntax.all.*
import com.pokeapp.api.dto.{ApiError, ResponseCodec}
import com.pokeapp.api.dto.ResponseCodec.given
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl

// Middleware que captura cualquier excepción no controlada que escape de las rutas
// y la convierte en una respuesta 500 con el formato de error estándar de la API.
// Sin esto, http4s devolvería un 500 sin cuerpo o con un mensaje no estructurado.
object ErrorHandler:
  def apply[F[_]: Concurrent](routes: HttpRoutes[F]): HttpRoutes[F] =
    val dsl = Http4sDsl[F]
    import dsl.*

    Kleisli: request =>
      routes(request).handleErrorWith: throwable =>
        OptionT.liftF(
          InternalServerError(ApiError.internalError(throwable.getMessage).asJson)
        )
