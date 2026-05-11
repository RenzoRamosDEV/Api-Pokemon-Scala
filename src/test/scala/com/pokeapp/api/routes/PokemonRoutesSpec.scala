package com.pokeapp.api.routes

import cats.effect.IO
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

  test("GET /api/v1/pokemon/{id} returns 404 for unknown pokemon"):
    val routes  = buildRoutes(repoWithPikachu).routes.orNotFound
    val request = Request[IO](Method.GET, uri"/api/v1/pokemon/9999")
    routes.run(request).map(r => assertEquals(r.status, Status.NotFound))

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

  test("GET /api/v1/pokemon?limit=0 returns 400"):
    val routes  = buildRoutes(repoWithPikachu).routes.orNotFound
    val request = Request[IO](Method.GET, uri"/api/v1/pokemon?limit=0&offset=0")
    routes.run(request).map(r => assertEquals(r.status, Status.BadRequest))

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

  test("GET /api/v1/pokemon?offset=-1 returns 400"):
    val routes  = buildRoutes(repoWithPikachu).routes.orNotFound
    val request = Request[IO](Method.GET, uri"/api/v1/pokemon?limit=20&offset=-1")
    routes.run(request).map(r => assertEquals(r.status, Status.BadRequest))

  // ── Endpoint full ─────────────────────────────────────────────────────────

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
