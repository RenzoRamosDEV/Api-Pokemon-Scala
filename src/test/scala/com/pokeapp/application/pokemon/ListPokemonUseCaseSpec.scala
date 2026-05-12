package com.pokeapp.application.pokemon

import cats.effect.IO
import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.PokemonRepository
import munit.CatsEffectSuite

class ListPokemonUseCaseSpec extends CatsEffectSuite:

  private val pagedResult = PaginatedResponse[NamedResource](
    count = 1302,
    next = Some("https://pokeapi.co/api/v2/pokemon?offset=20&limit=20"),
    previous = None,
    results = List(NamedResource("bulbasaur", "url"), NamedResource("ivysaur", "url"))
  )

  private def repoWith(
      result: Either[DomainError, PaginatedResponse[NamedResource]]
  ): PokemonRepository[IO] =
    new PokemonRepository[IO]:
      def findById(id: Int): IO[Either[DomainError, com.pokeapp.domain.model.Pokemon]] =
        IO.pure(Left(DomainError.NotFound("not found")))
      def findByName(name: String): IO[Either[DomainError, com.pokeapp.domain.model.Pokemon]] =
        IO.pure(Left(DomainError.NotFound("not found")))
      def list(
          limit: Int,
          offset: Int
      ): IO[Either[DomainError, PaginatedResponse[NamedResource]]] =
        IO.pure(result)

  test("execute returns paginated list of pokemon"):
    val repo    = repoWith(Right(pagedResult))
    val useCase = ListPokemonUseCase(repo)

    useCase
      .execute(limit = 20, offset = 0)
      .map: result =>
        assertEquals(result, Right(pagedResult))

  test("execute returns error when external api fails"):
    val error   = DomainError.ExternalApiError("service unavailable", 503)
    val repo    = repoWith(Left(error))
    val useCase = ListPokemonUseCase(repo)

    useCase
      .execute(limit = 20, offset = 0)
      .map: result =>
        assertEquals(result, Left(error))

  test("execute uses default limit and offset"):
    val repo    = repoWith(Right(pagedResult))
    val useCase = ListPokemonUseCase(repo)

    useCase
      .execute()
      .map: result =>
        assertEquals(result.map(_.count), Right(1302))
