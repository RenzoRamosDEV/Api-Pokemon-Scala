package com.pokeapp.api.routes

import cats.Monad
import io.circe.Json
import org.http4s.HttpRoutes
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl

// Endpoint de health check para uso con load balancers, contenedores y monitoreo.
// Devuelve 200 { "status": "UP" } si el servidor está levantado y aceptando requests.
// No verifica conectividad con PokeAPI ni estado del caché (health check shallow).
class HealthRoutes[F[_]: Monad]:
  private val dsl = Http4sDsl[F]
  import dsl.*

  val routes: HttpRoutes[F] = HttpRoutes.of[F]:
    case GET -> Root / "health" =>
      Ok(Json.obj("status" -> Json.fromString("UP")))
