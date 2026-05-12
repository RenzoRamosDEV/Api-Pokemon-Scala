package com.pokeapp.infrastructure.generic

import cats.effect.IO
import cats.effect.Ref
import cats.syntax.applicative.*
import com.pokeapp.config.PokeApiConfig
import com.pokeapp.domain.error.DomainError
import com.pokeapp.infrastructure.http.client.GenericPokeApiClient
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.client.Client

class GenericPokeApiClientSpec extends CatsEffectSuite:

  private val config = PokeApiConfig("https://pokeapi.co/api/v2", 10, 3)

  private def clientWith(status: Status, body: String): Client[IO] =
    Client.fromHttpApp(HttpApp[IO]: _ =>
      Response[IO](status = status).withEntity(body).pure[IO])

  test("getByIdOrName returns Json on 200"):
    val httpClient = clientWith(Status.Ok, """{"id": 1, "name": "stench"}""")
    val client     = GenericPokeApiClient[IO](httpClient, config)

    client
      .getByIdOrName("ability", "1")
      .map: result =>
        assert(result.isRight)
        result.foreach(json => assert(json.toString.contains("stench")))

  test("getByIdOrName returns NotFound on 404 with non-empty message"):
    val httpClient = clientWith(Status.NotFound, "")
    val client     = GenericPokeApiClient[IO](httpClient, config)

    client
      .getByIdOrName("ability", "9999")
      .map: result =>
        result match
          case Left(DomainError.NotFound(msg)) => assert(msg.nonEmpty)
          case other                           => fail(s"Expected NotFound, got $other")

  test("list returns paginated Json on 200"):
    val body =
      """{"count": 298, "next": null, "previous": null, "results": [{"name":"stench","url":"url"}]}"""
    val httpClient = clientWith(Status.Ok, body)
    val client     = GenericPokeApiClient[IO](httpClient, config)

    client
      .list("ability", 20, 0)
      .map: result =>
        assert(result.isRight)
        result.foreach(json => assert(json.toString.contains("298")))

  test("getByIdOrName returns RateLimitExceeded on 429"):
    val httpClient = clientWith(Status.TooManyRequests, "")
    val client     = GenericPokeApiClient[IO](httpClient, config)

    client
      .getByIdOrName("ability", "1")
      .map: result =>
        assertEquals(result, Left(DomainError.RateLimitExceeded))

  // Partición error de servidor: 500 → ExternalApiError con mensaje no vacío
  test("getByIdOrName returns ExternalApiError on 500 with non-empty message"):
    val httpClient = clientWith(Status.InternalServerError, "")
    val client     = GenericPokeApiClient[IO](httpClient, config)

    client
      .getByIdOrName("ability", "1")
      .map: result =>
        result match
          case Left(DomainError.ExternalApiError(msg, 500)) => assert(msg.nonEmpty)
          case other                                        => fail(s"Expected ExternalApiError(500) but got $other")

  // Verifica que list construye la URL con query params "limit" y "offset" correctos.
  // Sin este test, mutar esos strings no es detectado porque el stub ignora la URL.
  test("list sends request with correct query params"):
    val listBody = """{"count": 298, "next": null, "previous": null, "results": []}"""
    Ref.of[IO, Option[Uri]](None).flatMap: uriRef =>
      val capturingClient = Client.fromHttpApp(HttpApp[IO]: req =>
        uriRef.set(Some(req.uri)) *> Response[IO](status = Status.Ok).withEntity(listBody).pure[IO]
      )
      val client = GenericPokeApiClient[IO](capturingClient, config)
      client.list("ability", 10, 5) *> uriRef.get.map:
        case Some(uri) =>
          assertEquals(uri.query.params.get("limit"), Some("10"))
          assertEquals(uri.query.params.get("offset"), Some("5"))
        case None => fail("No request was made")

  // ── list ──────────────────────────────────────────────────────────────────
  // Particiones de equivalencia para `list`: mismas clases que getByIdOrName.

  test("list returns NotFound on 404"):
    val httpClient = clientWith(Status.NotFound, "")
    val client     = GenericPokeApiClient[IO](httpClient, config)

    client
      .list("ability", 20, 0)
      .map: result =>
        result match
          case Left(DomainError.NotFound(_)) => ()
          case other                         => fail(s"Expected NotFound but got $other")

  test("list returns RateLimitExceeded on 429"):
    val httpClient = clientWith(Status.TooManyRequests, "")
    val client     = GenericPokeApiClient[IO](httpClient, config)

    client
      .list("ability", 20, 0)
      .map: result =>
        assertEquals(result, Left(DomainError.RateLimitExceeded))
