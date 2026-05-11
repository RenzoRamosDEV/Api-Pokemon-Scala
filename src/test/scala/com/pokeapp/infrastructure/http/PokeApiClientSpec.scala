package com.pokeapp.infrastructure.http

import cats.effect.IO
import cats.syntax.applicative.*
import com.pokeapp.config.PokeApiConfig
import com.pokeapp.domain.error.DomainError
import com.pokeapp.infrastructure.http.client.PokeApiClient
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.client.Client

// Tests unitarios del PokeApiClient: verifican que los status HTTP de PokeAPI
// se traduzcan correctamente a los tipos de DomainError esperados,
// y que el JSON se deserialice al modelo de dominio correcto.
// Se usa Client.fromHttpApp para simular respuestas HTTP sin red real.
//
// Particiones de equivalencia para status HTTP:
//   [200]       clase éxito          → Right(modelo)
//   [404]       clase no encontrado  → Left(NotFound)
//   [429]       clase rate limit     → Left(RateLimitExceeded)
//   [5xx]       clase error servidor → Left(ExternalApiError)
class PokeApiClientSpec extends CatsEffectSuite:

  private val config = PokeApiConfig(
    baseUrl        = "https://pokeapi.co/api/v2",
    timeoutSeconds = 10,
    maxRetries     = 3
  )

  // JSON completo de Pikachu con todos los campos requeridos por el decoder
  private val pikachuJson = """{
    "id": 25,
    "name": "pikachu",
    "base_experience": 112,
    "height": 4,
    "weight": 60,
    "is_default": true,
    "abilities": [{"ability": {"name": "static", "url": "url"}, "is_hidden": false, "slot": 1}],
    "moves": [{"move": {"name": "thunder-shock", "url": "url"}}],
    "stats": [{"stat": {"name": "speed", "url": "url"}, "base_stat": 90, "effort": 2}],
    "types": [{"slot": 1, "type": {"name": "electric", "url": "url"}}],
    "sprites": {"front_default": "front.png", "front_shiny": null, "back_default": null, "back_shiny": null}
  }"""

  // Construye un Client stub que ignora la URL y siempre devuelve el mismo status/body
  private def clientReturning(status: Status, body: String): Client[IO] =
    Client.fromHttpApp(HttpApp[IO]: _ =>
      Response[IO](status = status).withEntity(body).pure[IO])

  // ── getPokemon ────────────────────────────────────────────────────────────

  test("getPokemon returns Pokemon on 200"):
    val apiClient = PokeApiClient[IO](clientReturning(Status.Ok, pikachuJson), config)
    apiClient.getPokemon("pikachu").map: result =>
      assertEquals(result.map(_.id), Right(25))
      assertEquals(result.map(_.name), Right("pikachu"))

  test("getPokemon returns NotFound on 404"):
    val apiClient = PokeApiClient[IO](clientReturning(Status.NotFound, ""), config)
    apiClient.getPokemon("unknown").map: result =>
      assert(result.isLeft)
      result match
        case Left(DomainError.NotFound(_)) => ()
        case other                         => fail(s"Expected NotFound but got $other")

  test("getPokemon returns RateLimitExceeded on 429"):
    val apiClient = PokeApiClient[IO](clientReturning(Status.TooManyRequests, ""), config)
    apiClient.getPokemon("pikachu").map: result =>
      assertEquals(result, Left(DomainError.RateLimitExceeded))

  // Partición 5xx: cualquier error del servidor se mapea a ExternalApiError
  test("getPokemon returns ExternalApiError on 500"):
    val apiClient = PokeApiClient[IO](clientReturning(Status.InternalServerError, ""), config)
    apiClient.getPokemon("pikachu").map: result =>
      result match
        case Left(DomainError.ExternalApiError(_, 500)) => ()
        case other                                      => fail(s"Expected ExternalApiError(500) but got $other")

  // ── listPokemon ───────────────────────────────────────────────────────────

  test("listPokemon returns paginated response on 200"):
    val listJson = """{
      "count": 1302,
      "next": "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20",
      "previous": null,
      "results": [{"name": "bulbasaur", "url": "url"}]
    }"""
    val apiClient = PokeApiClient[IO](clientReturning(Status.Ok, listJson), config)
    apiClient.listPokemon(20, 0).map: result =>
      assertEquals(result.map(_.count), Right(1302))
      assertEquals(result.map(_.results.length), Right(1))

  // ── getPokemonSpecies ─────────────────────────────────────────────────────

  // JSON mínimo válido de especie (flavor_text_entries vacío para simplicidad)
  private val speciesJson = """{
    "id": 25, "name": "pikachu",
    "is_baby": false, "is_legendary": false, "is_mythical": false,
    "capture_rate": 190, "base_happiness": 50, "gender_rate": 4,
    "flavor_text_entries": [],
    "evolution_chain": {"url": "https://pokeapi.co/api/v2/evolution-chain/10/"}
  }"""

  test("getPokemonSpecies returns PokemonSpecies on 200"):
    val apiClient = PokeApiClient[IO](clientReturning(Status.Ok, speciesJson), config)
    apiClient.getPokemonSpecies("pikachu").map: result =>
      assertEquals(result.map(_.id), Right(25))
      assertEquals(result.map(_.captureRate), Right(190))
      assertEquals(result.map(_.evolutionChain.map(_.url)), Right(Some("https://pokeapi.co/api/v2/evolution-chain/10/")))

  test("getPokemonSpecies returns NotFound on 404"):
    val apiClient = PokeApiClient[IO](clientReturning(Status.NotFound, ""), config)
    apiClient.getPokemonSpecies("unknown").map: result =>
      assert(result.isLeft)
      result match
        case Left(DomainError.NotFound(_)) => ()
        case other                         => fail(s"Expected NotFound but got $other")

  // Partición rate limit para species: misma clase que getPokemon 429
  test("getPokemonSpecies returns RateLimitExceeded on 429"):
    val apiClient = PokeApiClient[IO](clientReturning(Status.TooManyRequests, ""), config)
    apiClient.getPokemonSpecies("pikachu").map: result =>
      assertEquals(result, Left(DomainError.RateLimitExceeded))

  // Partición error de servidor para species
  test("getPokemonSpecies returns ExternalApiError on 500"):
    val apiClient = PokeApiClient[IO](clientReturning(Status.InternalServerError, ""), config)
    apiClient.getPokemonSpecies("pikachu").map: result =>
      result match
        case Left(DomainError.ExternalApiError(_, 500)) => ()
        case other                                      => fail(s"Expected ExternalApiError(500) but got $other")
