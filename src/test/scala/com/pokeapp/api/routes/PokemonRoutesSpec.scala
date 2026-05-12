package com.pokeapp.api.routes

import cats.effect.IO
import cats.syntax.applicative.*
import com.pokeapp.application.pokemon.{GetPokemonFullUseCase, GetPokemonUseCase, ListPokemonUseCase, PokemonFixtures}
import com.pokeapp.config.PokeApiConfig
import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.PokemonRepository
import com.pokeapp.infrastructure.http.client.PokeApiClient
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.client.Client
import org.http4s.implicits.*

// Tests de integración para las rutas HTTP de Pokémon.
// Se testea la capa de routing completa (parsing de path, query params, serialización JSON)
// usando repos/clientes stub en lugar de llamadas reales a PokeAPI.
class PokemonRoutesSpec extends CatsEffectSuite:

  // Cliente HTTP stub que siempre devuelve 404.
  // Se usa para GetPokemonFullUseCase en tests donde ese endpoint no es el foco,
  // para que las llamadas al full endpoint fallen limpiamente sin side effects.
  private val stubClient: Client[IO] = Client.fromHttpApp(
    HttpApp[IO](_ => IO.pure(Response[IO](Status.NotFound)))
  )
  private val stubPokeApiClient =
    PokeApiClient[IO](stubClient, PokeApiConfig("http://localhost", 5, 0))

  // Constructor de rutas con un repositorio inyectable para cada test
  private def buildRoutes(repo: PokemonRepository[IO]): PokemonRoutes[IO] =
    PokemonRoutes[IO](
      GetPokemonUseCase(repo),
      ListPokemonUseCase(repo),
      GetPokemonFullUseCase[IO](stubPokeApiClient)
    )

  // Repositorio in-memory que conoce solo a Pikachu (id=25, name="pikachu")
  private val repoWithPikachu: PokemonRepository[IO] = new PokemonRepository[IO]:
    def findById(id: Int): IO[Either[DomainError, com.pokeapp.domain.model.Pokemon]] =
      IO.pure(
        if id == 25 then Right(PokemonFixtures.pikachu)
        else Left(DomainError.NotFound(s"Pokemon $id not found"))
      )
    def findByName(name: String): IO[Either[DomainError, com.pokeapp.domain.model.Pokemon]] =
      IO.pure(
        if name == "pikachu" then Right(PokemonFixtures.pikachu)
        else Left(DomainError.NotFound(s"Pokemon $name not found"))
      )
    def list(limit: Int, offset: Int): IO[Either[DomainError, PaginatedResponse[NamedResource]]] =
      IO.pure(Right(PaginatedResponse(1302, None, None, List(NamedResource("pikachu", "url")))))

  // ── Búsqueda por ID ───────────────────────────────────────────────────────

  test("GET /api/v1/pokemon/{id} returns 200 with existing pokemon"):
    val routes  = buildRoutes(repoWithPikachu).routes.orNotFound
    val request = Request[IO](Method.GET, uri"/api/v1/pokemon/25")

    routes.run(request).flatMap: response =>
      assertEquals(response.status, Status.Ok)
      response.as[String].map: body =>
        assert(body.contains("pikachu"))

  test("GET /api/v1/pokemon/{name} returns 200 with existing pokemon by name"):
    val routes  = buildRoutes(repoWithPikachu).routes.orNotFound
    val request = Request[IO](Method.GET, uri"/api/v1/pokemon/pikachu")
    routes.run(request).map(r => assertEquals(r.status, Status.Ok))

  test("GET /api/v1/pokemon/{id} returns 404 with NOT_FOUND code for unknown pokemon"):
    val routes  = buildRoutes(repoWithPikachu).routes.orNotFound
    val request = Request[IO](Method.GET, uri"/api/v1/pokemon/9999")
    routes.run(request).flatMap: response =>
      assertEquals(response.status, Status.NotFound)
      response.as[String].map(body => assert(body.contains("NOT_FOUND")))

  // ── Listado paginado ──────────────────────────────────────────────────────

  test("GET /api/v1/pokemon returns 200 with paginated list"):
    val routes  = buildRoutes(repoWithPikachu).routes.orNotFound
    val request = Request[IO](Method.GET, uri"/api/v1/pokemon?limit=20&offset=0")

    routes.run(request).flatMap: response =>
      assertEquals(response.status, Status.Ok)
      response.as[String].map: body =>
        assert(body.contains("1302"))

  // ── Búsqueda por nombre inexistente ──────────────────────────────────────

  // Partición: nombre que no existe en el repositorio → 404
  // Complementa el test de búsqueda por nombre existente para cubrir ambas clases.
  test("GET /api/v1/pokemon/{name} returns 404 for unknown name"):
    val routes  = buildRoutes(repoWithPikachu).routes.orNotFound
    val request = Request[IO](Method.GET, uri"/api/v1/pokemon/mewtwo")
    routes.run(request).map(r => assertEquals(r.status, Status.NotFound))

  // ── Validación de parámetros de paginación ────────────────────────────────
  //
  // Particiones de equivalencia para `limit`:
  //   [< 1]   clase inválida baja  → 400  (representante: 0)
  //   [1..100] clase válida         → 200  (representantes frontera: 1 y 100)
  //   [> 100] clase inválida alta  → 400  (representante: 101)
  //
  // Particiones de equivalencia para `offset`:
  //   [< 0]  clase inválida → 400  (representante: -1)
  //   [>= 0] clase válida   → 200  (representante: 0)

  test("GET /api/v1/pokemon?limit=0 returns 400 with BAD_REQUEST code and message"):
    val routes  = buildRoutes(repoWithPikachu).routes.orNotFound
    val request = Request[IO](Method.GET, uri"/api/v1/pokemon?limit=0&offset=0")
    routes.run(request).flatMap: response =>
      assertEquals(response.status, Status.BadRequest)
      response.as[String].map: body =>
        assert(body.contains("BAD_REQUEST"))
        assert(body.contains("limit must be between 1 and 100"))

  test("GET /api/v1/pokemon?limit=1 returns 200 (valor frontera mínimo válido)"):
    val routes  = buildRoutes(repoWithPikachu).routes.orNotFound
    val request = Request[IO](Method.GET, uri"/api/v1/pokemon?limit=1&offset=0")
    routes.run(request).map(r => assertEquals(r.status, Status.Ok))

  test("GET /api/v1/pokemon?limit=100 returns 200 (valor frontera máximo válido)"):
    val routes  = buildRoutes(repoWithPikachu).routes.orNotFound
    val request = Request[IO](Method.GET, uri"/api/v1/pokemon?limit=100&offset=0")
    routes.run(request).map(r => assertEquals(r.status, Status.Ok))

  test("GET /api/v1/pokemon?limit=101 returns 400"):
    val routes  = buildRoutes(repoWithPikachu).routes.orNotFound
    val request = Request[IO](Method.GET, uri"/api/v1/pokemon?limit=101&offset=0")
    routes.run(request).map(r => assertEquals(r.status, Status.BadRequest))

  test("GET /api/v1/pokemon?offset=-1 returns 400 with message"):
    val routes  = buildRoutes(repoWithPikachu).routes.orNotFound
    val request = Request[IO](Method.GET, uri"/api/v1/pokemon?limit=20&offset=-1")
    routes.run(request).flatMap: response =>
      assertEquals(response.status, Status.BadRequest)
      response.as[String].map(body => assert(body.contains("offset must be >= 0")))

  // ── Endpoint full ─────────────────────────────────────────────────────────

  // JSONs mínimos para el endpoint /full (pokemon + species + evolution chain)
  private val pikachuJsonFull = """{
    "id": 25, "name": "pikachu", "base_experience": 112, "height": 4, "weight": 60,
    "is_default": true, "abilities": [], "moves": [], "stats": [],
    "types": [{"slot": 1, "type": {"name": "electric", "url": "url"}}],
    "sprites": {"front_default": "front.png", "front_shiny": null, "back_default": null, "back_shiny": null}
  }"""
  private val speciesJsonFull = """{
    "id": 25, "name": "pikachu", "is_baby": false, "is_legendary": false, "is_mythical": false,
    "capture_rate": 190, "base_happiness": 50, "gender_rate": 4,
    "flavor_text_entries": [],
    "evolution_chain": {"url": "https://pokeapi.co/api/v2/evolution-chain/10/"}
  }"""
  private val evolutionJsonFull = """{
    "id": 10, "baby_trigger_item": null,
    "chain": {"is_baby": false, "species": {"name": "pichu", "url": "url"}, "evolution_details": [], "evolves_to": []}
  }"""

  // Stub que enruta por path — necesario para testear el endpoint /full con datos reales.
  // Sin este test que retorna 200, mutar los strings de la ruta ("api","v1","pokemon","full")
  // no era detectado porque el único test de /full esperaba 404 en ambos casos.
  private def fullSupportClient: Client[IO] =
    Client.fromHttpApp(HttpApp[IO]: req =>
      val path = req.uri.path.renderString
      val (status, body) =
        if path.endsWith("/pokemon/pikachu")         then (Status.Ok, pikachuJsonFull)
        else if path.endsWith("/pokemon-species/pikachu") then (Status.Ok, speciesJsonFull)
        else if path.contains("evolution-chain")         then (Status.Ok, evolutionJsonFull)
        else (Status.NotFound, "")
      Response[IO](status = status).withEntity(body).pure[IO]
    )

  test("GET /api/v1/pokemon/{name}/full returns 200 with combined data"):
    val fullPokeApiClient = PokeApiClient[IO](fullSupportClient, PokeApiConfig("https://pokeapi.co/api/v2", 5, 0))
    val routes = PokemonRoutes[IO](
      GetPokemonUseCase(repoWithPikachu),
      ListPokemonUseCase(repoWithPikachu),
      GetPokemonFullUseCase[IO](fullPokeApiClient)
    ).routes.orNotFound
    val request = Request[IO](Method.GET, uri"/api/v1/pokemon/pikachu/full")

    routes.run(request).flatMap: response =>
      assertEquals(response.status, Status.Ok)
      response.as[String].map: body =>
        assert(body.contains("pikachu"))
        assert(body.contains("captureRate") || body.contains("capture_rate") || body.contains("190"))

  // El stub devuelve 404 para PokeAPI, por lo tanto el pokemon 9999 no se encuentra
  test("GET /api/v1/pokemon/{id}/full returns 404 when pokemon not found"):
    val routes  = buildRoutes(repoWithPikachu).routes.orNotFound
    val request = Request[IO](Method.GET, uri"/api/v1/pokemon/9999/full")
    routes.run(request).map(r => assertEquals(r.status, Status.NotFound))

  // ── Health check ──────────────────────────────────────────────────────────

  test("GET /health returns 200"):
    val healthRoutes = HealthRoutes[IO].routes.orNotFound
    val request      = Request[IO](Method.GET, uri"/health")

    healthRoutes.run(request).flatMap: response =>
      assertEquals(response.status, Status.Ok)
      response.as[String].map: body =>
        assert(body.contains("UP"))
