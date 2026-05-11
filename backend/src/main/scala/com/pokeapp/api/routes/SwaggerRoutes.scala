package com.pokeapp.api.routes

import cats.effect.Async
import com.pokeapp.api.ResourceRegistry
import io.circe.Json
import io.circe.syntax.*
import org.http4s.{HttpRoutes, MediaType}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.server.Router

class SwaggerRoutes[F[_]: Async]:
  private val dsl = Http4sDsl[F]
  import dsl.*

  private val paginationParameters: Json = Json.arr(
    Json.obj(
      "name" -> "limit".asJson,
      "in" -> "query".asJson,
      "description" -> "Number of items to return (default: 20)".asJson,
      "schema" -> Json.obj("type" -> "integer".asJson, "default" -> 20.asJson).asJson
    ).asJson,
    Json.obj(
      "name" -> "offset".asJson,
      "in" -> "query".asJson,
      "description" -> "Number of items to skip (default: 0)".asJson,
      "schema" -> Json.obj("type" -> "integer".asJson, "default" -> 0.asJson).asJson
    ).asJson
  )

  private def resourceLabel(resource: String): String =
    resource
      .split("-")
      .map(part => if part.isEmpty then part else part.head.toUpper + part.drop(1))
      .mkString(" ")

  private def listOperation(label: String): Json =
    Json.obj(
      "summary" -> s"List $label".asJson,
      "description" -> s"Returns a paginated list of $label resources".asJson,
      "tags" -> Json.arr("Resources".asJson).asJson,
      "parameters" -> paginationParameters.asJson,
      "responses" -> Json.obj(
        "200" -> Json.obj("description" -> s"List of $label".asJson).asJson,
        "429" -> Json.obj("description" -> "Rate limit exceeded".asJson).asJson,
        "502" -> Json.obj("description" -> "External API error".asJson).asJson
      ).asJson
    )

  private def getOperation(label: String): Json =
    Json.obj(
      "summary" -> s"Get $label by ID or name".asJson,
      "description" -> s"Returns details for a $label resource".asJson,
      "tags" -> Json.arr("Resources".asJson).asJson,
      "parameters" -> Json.arr(
        Json.obj(
          "name" -> "idOrName".asJson,
          "in" -> "path".asJson,
          "required" -> true.asJson,
          "description" -> "Resource ID (integer) or name (string)".asJson,
          "schema" -> Json.obj(
            "oneOf" -> Json.arr(
              Json.obj("type" -> "integer".asJson).asJson,
              Json.obj("type" -> "string".asJson).asJson
            ).asJson
          ).asJson
        ).asJson
      ).asJson,
      "responses" -> Json.obj(
        "200" -> Json.obj("description" -> s"$label details".asJson).asJson,
        "404" -> Json.obj("description" -> "Resource not found".asJson).asJson,
        "429" -> Json.obj("description" -> "Rate limit exceeded".asJson).asJson,
        "502" -> Json.obj("description" -> "External API error".asJson).asJson
      ).asJson
    )

  private val genericPaths: List[(String, Json)] =
    ResourceRegistry.all.keys.toList
      .filterNot(_ == "pokemon")
      .sorted
      .flatMap: resource =>
        val label = resourceLabel(resource)
        List(
          s"/api/v1/$resource" -> Json.obj("get" -> listOperation(label).asJson),
          s"/api/v1/$resource/{idOrName}" -> Json.obj("get" -> getOperation(label).asJson)
        )

  // OpenAPI specification for the API
  private val openApiSpec: Json = Json.obj(
    "openapi" -> "3.0.0".asJson,
    "info" -> Json.obj(
      "title" -> "PokeAPI Scala Wrapper".asJson,
      "description" -> "A Scala wrapper API for the PokeAPI service with caching and rate limiting".asJson,
      "version" -> "1.0.0".asJson,
      "contact" -> Json.obj(
        "name" -> "PokeAPI Scala".asJson
      ).asJson
    ).asJson,
    "servers" -> Json.arr(
      Json.obj("url" -> "/".asJson, "description" -> "Local server".asJson).asJson
    ),
    "paths" -> Json.obj(
      (
        List[(String, Json)](
          "/health" -> Json.obj(
        "get" -> Json.obj(
          "summary" -> "Health check endpoint".asJson,
          "description" -> "Returns the health status of the API".asJson,
          "tags" -> Json.arr("Health".asJson).asJson,
          "responses" -> Json.obj(
            "200" -> Json.obj(
              "description" -> "API is healthy".asJson,
              "content" -> Json.obj(
                "application/json" -> Json.obj(
                  "schema" -> Json.obj(
                    "type" -> "object".asJson,
                    "properties" -> Json.obj(
                      "status" -> Json.obj("type" -> "string".asJson, "example" -> "UP".asJson).asJson
                    ).asJson
                  ).asJson
                ).asJson
              ).asJson
            ).asJson
          ).asJson
        ).asJson
      ).asJson,
          "/api/v1/pokemon" -> Json.obj(
        "get" -> Json.obj(
          "summary" -> "List all Pokemon".asJson,
          "description" -> "Returns a paginated list of Pokemon".asJson,
          "tags" -> Json.arr("Pokemon".asJson).asJson,
          "parameters" -> paginationParameters.asJson,
          "responses" -> Json.obj(
            "200" -> Json.obj(
              "description" -> "List of Pokemon".asJson,
              "content" -> Json.obj(
                "application/json" -> Json.obj(
                  "schema" -> Json.obj(
                    "type" -> "object".asJson,
                    "properties" -> Json.obj(
                      "data" -> Json.obj(
                        "type" -> "object".asJson,
                        "properties" -> Json.obj(
                          "count" -> Json.obj("type" -> "integer".asJson).asJson,
                          "next" -> Json.obj("type" -> "string".asJson, "nullable" -> true.asJson).asJson,
                          "previous" -> Json.obj("type" -> "string".asJson, "nullable" -> true.asJson).asJson,
                          "results" -> Json.obj(
                            "type" -> "array".asJson,
                            "items" -> Json.obj(
                              "type" -> "object".asJson,
                              "properties" -> Json.obj(
                                "name" -> Json.obj("type" -> "string".asJson).asJson,
                                "url" -> Json.obj("type" -> "string".asJson).asJson
                              ).asJson
                            ).asJson
                          ).asJson
                        ).asJson
                      ).asJson,
                      "success" -> Json.obj("type" -> "boolean".asJson).asJson,
                      "timestamp" -> Json.obj("type" -> "string".asJson, "format" -> "date-time".asJson).asJson
                    ).asJson
                  ).asJson
                ).asJson
              ).asJson
            ).asJson,
            "429" -> Json.obj("description" -> "Rate limit exceeded".asJson).asJson,
            "502" -> Json.obj("description" -> "External API error".asJson).asJson
          ).asJson
        ).asJson
      ).asJson,
          "/api/v1/pokemon/{idOrName}" -> Json.obj(
        "get" -> Json.obj(
          "summary" -> "Get Pokemon by ID or name".asJson,
          "description" -> "Returns detailed information about a Pokemon".asJson,
          "tags" -> Json.arr("Pokemon".asJson).asJson,
          "parameters" -> Json.arr(
            Json.obj(
              "name" -> "idOrName".asJson,
              "in" -> "path".asJson,
              "required" -> true.asJson,
              "description" -> "Pokemon ID (integer) or name (string)".asJson,
              "schema" -> Json.obj(
                "oneOf" -> Json.arr(
                  Json.obj("type" -> "integer".asJson).asJson,
                  Json.obj("type" -> "string".asJson).asJson
                ).asJson
              ).asJson
            ).asJson
          ).asJson,
          "responses" -> Json.obj(
            "200" -> Json.obj(
              "description" -> "Pokemon details".asJson,
              "content" -> Json.obj(
                "application/json" -> Json.obj(
                  "schema" -> Json.obj(
                    "type" -> "object".asJson,
                    "properties" -> Json.obj(
                      "data" -> Json.obj(
                        "type" -> "object".asJson,
                        "properties" -> Json.obj(
                          "id" -> Json.obj("type" -> "integer".asJson).asJson,
                          "name" -> Json.obj("type" -> "string".asJson).asJson,
                          "types" -> Json.obj(
                            "type" -> "array".asJson,
                            "items" -> Json.obj(
                              "type" -> "object".asJson,
                              "properties" -> Json.obj(
                                "slot" -> Json.obj("type" -> "integer".asJson).asJson,
                                "type" -> Json.obj(
                                  "type" -> "object".asJson,
                                  "properties" -> Json.obj(
                                    "name" -> Json.obj("type" -> "string".asJson).asJson,
                                    "url" -> Json.obj("type" -> "string".asJson).asJson
                                  ).asJson
                                ).asJson
                              ).asJson
                            ).asJson
                          ).asJson,
                          "stats" -> Json.obj(
                            "type" -> "array".asJson,
                            "items" -> Json.obj(
                              "type" -> "object".asJson,
                              "properties" -> Json.obj(
                                "base_stat" -> Json.obj("type" -> "integer".asJson).asJson,
                                "effort" -> Json.obj("type" -> "integer".asJson).asJson,
                                "stat" -> Json.obj(
                                  "type" -> "object".asJson,
                                  "properties" -> Json.obj(
                                    "name" -> Json.obj("type" -> "string".asJson).asJson,
                                    "url" -> Json.obj("type" -> "string".asJson).asJson
                                  ).asJson
                                ).asJson
                              ).asJson
                            ).asJson
                          ).asJson,
                          "abilities" -> Json.obj(
                            "type" -> "array".asJson,
                            "items" -> Json.obj(
                              "type" -> "object".asJson,
                              "properties" -> Json.obj(
                                "ability" -> Json.obj(
                                  "type" -> "object".asJson,
                                  "properties" -> Json.obj(
                                    "name" -> Json.obj("type" -> "string".asJson).asJson,
                                    "url" -> Json.obj("type" -> "string".asJson).asJson
                                  ).asJson
                                ).asJson,
                                "is_hidden" -> Json.obj("type" -> "boolean".asJson).asJson,
                                "slot" -> Json.obj("type" -> "integer".asJson).asJson
                              ).asJson
                            ).asJson
                          ).asJson
                        ).asJson
                      ).asJson,
                      "success" -> Json.obj("type" -> "boolean".asJson).asJson,
                      "timestamp" -> Json.obj("type" -> "string".asJson, "format" -> "date-time".asJson).asJson
                    ).asJson
                  ).asJson
                ).asJson
              ).asJson
            ).asJson,
            "404" -> Json.obj("description" -> "Pokemon not found".asJson).asJson,
            "429" -> Json.obj("description" -> "Rate limit exceeded".asJson).asJson,
            "502" -> Json.obj("description" -> "External API error".asJson).asJson
          ).asJson
        ).asJson
      ).asJson
        ) ++ genericPaths
      ).map((path, spec) => path -> spec.asJson)*
    ).asJson,
    "tags" -> Json.arr(
      Json.obj(
        "name" -> "Health".asJson,
        "description" -> "Health check operations".asJson
      ).asJson,
      Json.obj(
        "name" -> "Pokemon".asJson,
        "description" -> "Pokemon-related operations".asJson
      ).asJson,
      Json.obj(
        "name" -> "Resources".asJson,
        "description" -> "Generic resource operations".asJson
      ).asJson
    ).asJson
  )

  // Route for the OpenAPI spec
  private val specRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "swagger.json" =>
      Ok(openApiSpec.noSpaces, `Content-Type`(MediaType.application.json))
  }

  // Route for Swagger UI - serve the swagger-ui webjar
  val routes: HttpRoutes[F] = Router(
    "/swagger" -> specRoute,
    "/swagger-ui" -> swaggerUiRoutes
  )

  // Swagger UI static content from webjar
  private def swaggerUiRoutes: HttpRoutes[F] = {
    // Serve the swagger-ui index.html with our spec URL
    val indexHtml = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PokeAPI Scala - Swagger UI</title>
    <link rel="stylesheet" type="text/css" href="https://unpkg.com/swagger-ui-dist@5.9.0/swagger-ui.css" />
</head>
<body>
    <div id="swagger-ui"></div>
    <script src="https://unpkg.com/swagger-ui-dist@5.9.0/swagger-ui-bundle.js" crossorigin></script>
    <script src="https://unpkg.com/swagger-ui-dist@5.9.0/swagger-ui-standalone-preset.js" crossorigin></script>
    <script>
        window.onload = function() {
            window.ui = SwaggerUIBundle({
                url: '/swagger/swagger.json',
                dom_id: '#swagger-ui',
                deepLinking: true,
                presets: [
                    SwaggerUIBundle.presets.apis,
                    SwaggerUIStandalonePreset
                ],
                plugins: [
                    SwaggerUIBundle.plugins.DownloadUrl
                ],
                layout: "StandaloneLayout"
            });
        };
    </script>
</body>
</html>"""

    HttpRoutes.of[F] {
      case GET -> Root / "" | GET -> Root =>
        Ok(indexHtml, `Content-Type`(MediaType.text.html))
    }
  }
