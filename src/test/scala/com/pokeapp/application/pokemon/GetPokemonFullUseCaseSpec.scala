package com.pokeapp.application.pokemon

import cats.effect.IO
import cats.syntax.applicative.*
import com.pokeapp.config.PokeApiConfig
import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.*
import com.pokeapp.infrastructure.http.client.PokeApiClient
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.client.Client

// Tests del caso de uso enriquecido que combina pokemon + species + evolution chain.
// Se usan clientes HTTP stub que inspeccionan el path de la request para devolver
// el JSON mock correspondiente, evitando llamadas reales a PokeAPI.
class GetPokemonFullUseCaseSpec extends CatsEffectSuite:

  private val config = PokeApiConfig("https://pokeapi.co/api/v2", 5, 0)

  // JSONs mínimos pero válidos que los decoders de dominio pueden deserializar correctamente
  private val pikachuSpeciesJson = """{
    "id": 25,
    "name": "pikachu",
    "is_baby": false,
    "is_legendary": false,
    "is_mythical": false,
    "capture_rate": 190,
    "base_happiness": 50,
    "gender_rate": 4,
    "flavor_text_entries": [
      {"flavor_text": "When several of these\nPOKéMON gather, their\nelectricity could...",
       "language": {"name": "en", "url": "url"},
       "version": {"name": "red", "url": "url"}}
    ],
    "evolution_chain": {"url": "https://pokeapi.co/api/v2/evolution-chain/10/"}
  }"""

  private val evolutionChainJson = """{
    "id": 10,
    "baby_trigger_item": null,
    "chain": {
      "is_baby": false,
      "species": {"name": "pichu", "url": "url"},
      "evolution_details": [],
      "evolves_to": []
    }
  }"""

  private val pikachuJson = """{
    "id": 25, "name": "pikachu", "base_experience": 112, "height": 4, "weight": 60,
    "is_default": true, "abilities": [], "moves": [], "stats": [],
    "types": [{"slot": 1, "type": {"name": "electric", "url": "url"}}],
    "sprites": {"front_default": "front.png", "front_shiny": null, "back_default": null, "back_shiny": null}
  }"""

  // Construye un Client stub que enruta por path de la request.
  // Si el path no hace match en `responses`, devuelve 404 por defecto.
  private def clientWith(responses: PartialFunction[String, (Status, String)]): Client[IO] =
    Client.fromHttpApp(HttpApp[IO]: req =>
      val path           = req.uri.path.renderString
      val (status, body) = responses.applyOrElse(path, (_: String) => (Status.NotFound, ""))
      Response[IO](status = status).withEntity(body).pure[IO]
    )

  // ── Flujo exitoso ─────────────────────────────────────────────────────────

  test("execute returns PokemonFull combining pokemon + species + evolution chain"):
    val client = clientWith:
      case p if p.endsWith("/pokemon/pikachu")         => (Status.Ok, pikachuJson)
      case p if p.endsWith("/pokemon-species/pikachu") => (Status.Ok, pikachuSpeciesJson)
      case p if p.contains("evolution-chain")          => (Status.Ok, evolutionChainJson)
    val useCase = GetPokemonFullUseCase[IO](PokeApiClient[IO](client, config))

    useCase.execute("pikachu").map: result =>
      assert(result.isRight)
      val full = result.toOption.get
      assertEquals(full.pokemon.name, "pikachu")
      assertEquals(full.species.name, "pikachu")
      assertEquals(full.species.captureRate, 190)
      assert(full.evolutionChain.isDefined)
      assertEquals(full.evolutionChain.get.id, 10)

  // ── Fallos en cascada ─────────────────────────────────────────────────────

  test("execute returns Left when pokemon fetch fails"):
    // Si el primer paso falla, se propaga el error sin llamar a species ni evolution
    val client  = clientWith:
      case p if p.endsWith("/pokemon/unknown") => (Status.NotFound, "")
    val useCase = GetPokemonFullUseCase[IO](PokeApiClient[IO](client, config))

    useCase.execute("unknown").map: result =>
      assert(result.isLeft)
      result match
        case Left(DomainError.NotFound(_)) => ()
        case other                         => fail(s"Expected NotFound but got $other")

  test("execute returns Left when species fetch fails"):
    // Pokemon existe pero species falla → error propagado (species es dato crítico)
    val client = clientWith:
      case p if p.endsWith("/pokemon/pikachu")         => (Status.Ok, pikachuJson)
      case p if p.endsWith("/pokemon-species/pikachu") => (Status.NotFound, "")
    val useCase = GetPokemonFullUseCase[IO](PokeApiClient[IO](client, config))

    useCase.execute("pikachu").map(result => assert(result.isLeft))

  test("execute returns PokemonFull with None evolution chain when chain fetch fails"):
    // Evolution chain es dato secundario: si falla se devuelve PokemonFull con None en lugar de error
    val client = clientWith:
      case p if p.endsWith("/pokemon/pikachu")         => (Status.Ok, pikachuJson)
      case p if p.endsWith("/pokemon-species/pikachu") => (Status.Ok, pikachuSpeciesJson)
      case p if p.contains("evolution-chain")          => (Status.NotFound, "")
    val useCase = GetPokemonFullUseCase[IO](PokeApiClient[IO](client, config))

    useCase.execute("pikachu").map: result =>
      assert(result.isRight)
      assertEquals(result.toOption.get.evolutionChain, None)
