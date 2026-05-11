package com.pokeapp.application.pokemon

import cats.effect.IO
import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.PokemonRepository
import munit.CatsEffectSuite

class GetPokemonUseCaseSpec extends CatsEffectSuite:

  private def repoReturning(
      result: Either[DomainError, com.pokeapp.domain.model.Pokemon]
  ): PokemonRepository[IO] =
    new PokemonRepository[IO]:
      def findById(id: Int): IO[Either[DomainError, com.pokeapp.domain.model.Pokemon]] =
        IO.pure(result)
      def findByName(name: String): IO[Either[DomainError, com.pokeapp.domain.model.Pokemon]] =
        IO.pure(result)
      def list(
          limit: Int,
          offset: Int
      ): IO[Either[DomainError, PaginatedResponse[NamedResource]]] =
        IO.pure(Right(PaginatedResponse(0, None, None, Nil)))

  test("execute returns a Pokemon when it exists"):
    val repo    = repoReturning(Right(PokemonFixtures.pikachu))
    val useCase = GetPokemonUseCase(repo)

    useCase
      .execute(25)
      .map: result =>
        assertEquals(result, Right(PokemonFixtures.pikachu))

  test("execute returns NotFound when Pokemon does not exist"):
    val error   = DomainError.NotFound("Pokemon 9999 not found")
    val repo    = repoReturning(Left(error))
    val useCase = GetPokemonUseCase(repo)

    useCase
      .execute(9999)
      .map: result =>
        assertEquals(result, Left(error))

  test("executeByName returns a Pokemon when it exists"):
    val repo    = repoReturning(Right(PokemonFixtures.pikachu))
    val useCase = GetPokemonUseCase(repo)

    useCase
      .executeByName("pikachu")
      .map: result =>
        assertEquals(result, Right(PokemonFixtures.pikachu))
