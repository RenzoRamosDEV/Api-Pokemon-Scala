package com.pokeapp.api.middleware

import cats.effect.Async
import org.http4s.HttpRoutes
import org.http4s.server.middleware.Logger as Http4sLogger

// Middleware de logging para todas las requests y responses HTTP.
// logHeaders = false y logBody = false para evitar loguear datos sensibles y reducir ruido.
// Solo se loguea el método, path y status code de cada request.
object RequestLogger:
  def apply[F[_]: Async](routes: HttpRoutes[F]): HttpRoutes[F] =
    Http4sLogger.httpRoutes(logHeaders = false, logBody = false)(routes)
