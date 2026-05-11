package com.pokeapp.api

import cats.effect.{Async, Resource}
import cats.syntax.all.*
import com.comcast.ip4s.*
import com.pokeapp.api.middleware.{ErrorHandler, RequestLogger}
import com.pokeapp.api.routes.{
  GenericResourceRoutes,
  HealthRoutes,
  PokemonRoutes,
  SwaggerRoutes,
  TypedResourceRoutes
}
import com.pokeapp.config.ServerConfig
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.server.middleware.CORS
import org.typelevel.log4cats.Logger

object Server:
  def build[F[_]: Async: Logger](
      config: ServerConfig,
      pokemonRoutes: PokemonRoutes[F],
      typedRoutes: TypedResourceRoutes[F],
      genericRoutes: GenericResourceRoutes[F],
      healthRoutes: HealthRoutes[F],
      swaggerRoutes: SwaggerRoutes[F]
  ): Resource[F, Server] =

    // Orden de montaje importa: http4s evalúa rutas de arriba a abajo y toma la primera que hace match.
    // SwaggerRoutes y HealthRoutes primero para que no sean interceptadas por las rutas genéricas.
    // PokemonRoutes antes de GenericResourceRoutes para que /pokemon/{id}/full sea manejado aquí.
    val baseRoutes: HttpRoutes[F] =
      swaggerRoutes.routes
        <+> healthRoutes.routes
        <+> typedRoutes.routes
        <+> pokemonRoutes.routes
        <+> genericRoutes.routes

    // Stack de middleware aplicado de adentro hacia afuera:
    //   1. RequestLogger   → loguea cada request/response
    //   2. ErrorHandler    → captura excepciones no controladas → 500
    //   3. CORS            → añade headers Access-Control-Allow-Origin a todas las respuestas
    val routes: HttpRoutes[F] =
      CORS.policy.withAllowOriginAll(
        ErrorHandler(RequestLogger(baseRoutes))
      )

    // orNotFound convierte HttpRoutes[F] (que puede no matchear) en HttpApp[F] (siempre responde).
    // Si ninguna ruta matchea, devuelve 404 automáticamente.
    val host = Host.fromString(config.host).getOrElse(host"0.0.0.0")
    val port = Port.fromInt(config.port).getOrElse(port"8080")

    EmberServerBuilder
      .default[F]
      .withHost(host)
      .withPort(port)
      .withHttpApp(routes.orNotFound)
      .build
