package com.pokeapp.infrastructure.generic

import cats.effect.IO
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

  test("getByIdOrName returns NotFound on 404"):
    val httpClient = clientWith(Status.NotFound, "")
    val client     = GenericPokeApiClient[IO](httpClient, config)

    client
      .getByIdOrName("ability", "9999")
      .map: result =>
        result match
          case Left(DomainError.NotFound(_)) => ()
          case other                         => fail(s"Expected NotFound, got $other")

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

  test("returns RateLimitExceeded on 429"):
    val httpClient = clientWith(Status.TooManyRequests, "")
    val client     = GenericPokeApiClient[IO](httpClient, config)

    client
      .getByIdOrName("ability", "1")
      .map: result =>
        assertEquals(result, Left(DomainError.RateLimitExceeded))
