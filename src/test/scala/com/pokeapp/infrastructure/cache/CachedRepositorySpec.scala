package com.pokeapp.infrastructure.cache

import cats.effect.IO
import cats.effect.Ref
import com.github.blemale.scaffeine.Scaffeine
import com.pokeapp.application.pokemon.PokemonFixtures
import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.Pokemon
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.PokemonRepository
import munit.CatsEffectSuite

// Tests del comportamiento de CachedPokemonRepository:
// verifica que el caché efectivamente reduce las llamadas al repositorio subyacente
// y que los errores no se cachean (para que reintentos puedan recuperarse).
class CachedRepositorySpec extends CatsEffectSuite:

  // Repositorio fake que cuenta cuántas veces se llama a findById/findByName
  // usando un Ref[IO, Int] como contador thread-safe.
  private def buildRepo(
      callCounter: Ref[IO, Int],
      result: Either[DomainError, Pokemon]
  ): PokemonRepository[IO] =
    new PokemonRepository[IO]:
      def findById(id: Int): IO[Either[DomainError, Pokemon]] =
        callCounter.update(_ + 1) *> IO.pure(result)
      def findByName(name: String): IO[Either[DomainError, Pokemon]] =
        callCounter.update(_ + 1) *> IO.pure(result)
      def list(limit: Int, offset: Int): IO[Either[DomainError, PaginatedResponse[NamedResource]]] =
        IO.pure(Right(PaginatedResponse(0, None, None, Nil)))

  test("findById hits underlying only once for repeated calls"):
    // Tres llamadas con el mismo ID deben resultar en solo una llamada al subyacente
    for
      counter    <- Ref.of[IO, Int](0)
      underlying  = buildRepo(counter, Right(PokemonFixtures.pikachu))
      cache       = Scaffeine().build[String, Pokemon]()
      repo        = CachedPokemonRepository[IO](underlying, cache)
      _          <- repo.findById(25)
      _          <- repo.findById(25)
      _          <- repo.findById(25)
      calls      <- counter.get
    yield assertEquals(calls, 1)

  test("findById does not cache errors"):
    // Dos llamadas fallidas deben resultar en dos llamadas al subyacente
    // (los errores no se guardan en caché para permitir recuperación)
    for
      counter    <- Ref.of[IO, Int](0)
      underlying  = buildRepo(counter, Left(DomainError.NotFound("not found")))
      cache       = Scaffeine().build[String, Pokemon]()
      repo        = CachedPokemonRepository[IO](underlying, cache)
      _          <- repo.findById(9999)
      _          <- repo.findById(9999)
      calls      <- counter.get
    yield assertEquals(calls, 2)

  // ── findByName ────────────────────────────────────────────────────────────
  // Partición equivalente a findById pero con clave "name:pikachu".
  // Se testea por separado porque usa una clave distinta en el caché; un bug
  // en el prefijo de clave podría hacer que findById y findByName compartan slot.

  test("findByName hits underlying only once for repeated calls"):
    for
      counter    <- Ref.of[IO, Int](0)
      underlying  = buildRepo(counter, Right(PokemonFixtures.pikachu))
      cache       = Scaffeine().build[String, Pokemon]()
      repo        = CachedPokemonRepository[IO](underlying, cache)
      _          <- repo.findByName("pikachu")
      _          <- repo.findByName("pikachu")
      _          <- repo.findByName("pikachu")
      calls      <- counter.get
    yield assertEquals(calls, 1)

  test("findByName does not cache errors"):
    for
      counter    <- Ref.of[IO, Int](0)
      underlying  = buildRepo(counter, Left(DomainError.NotFound("not found")))
      cache       = Scaffeine().build[String, Pokemon]()
      repo        = CachedPokemonRepository[IO](underlying, cache)
      _          <- repo.findByName("unknown")
      _          <- repo.findByName("unknown")
      calls      <- counter.get
    yield assertEquals(calls, 2)
