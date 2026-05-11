package com.pokeapp.application.evolution

import cats.effect.IO
import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.{ChainLink, EvolutionChain}
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.ResourceRepository
import munit.CatsEffectSuite

class GetEvolutionChainUseCaseSpec extends CatsEffectSuite:

  private val pikachuChain = EvolutionChain(
    id = 10,
    babyTriggerItem = None,
    chain = ChainLink(
      isBaby = false,
      species = NamedResource("pichu", "url"),
      evolutionDetails = Nil,
      evolvesTo = List(
        ChainLink(
          isBaby = false,
          species = NamedResource("pikachu", "url"),
          evolutionDetails = Nil,
          evolvesTo = List(
            ChainLink(
              isBaby = false,
              species = NamedResource("raichu", "url"),
              evolutionDetails = Nil,
              evolvesTo = Nil
            )
          )
        )
      )
    )
  )

  private def repo(
      result: Either[DomainError, EvolutionChain]
  ): ResourceRepository[IO, EvolutionChain] =
    new ResourceRepository[IO, EvolutionChain]:
      def findById(id: Int): IO[Either[DomainError, EvolutionChain]]        = IO.pure(result)
      def findByName(name: String): IO[Either[DomainError, EvolutionChain]] = IO.pure(result)
      def list(limit: Int, offset: Int): IO[Either[DomainError, PaginatedResponse[NamedResource]]] =
        IO.pure(Right(PaginatedResponse(0, None, None, Nil)))

  test("execute returns full evolution chain tree"):
    val useCase = GetEvolutionChainUseCase(repo(Right(pikachuChain)))
    useCase
      .execute(10)
      .map: r =>
        assertEquals(r.map(_.id), Right(10))
        assertEquals(r.map(_.chain.species.name), Right("pichu"))
        assertEquals(r.map(_.chain.evolvesTo.head.species.name), Right("pikachu"))

  test("execute returns NotFound for unknown chain"):
    val err     = DomainError.NotFound("EvolutionChain 9999 not found")
    val useCase = GetEvolutionChainUseCase(repo(Left(err)))
    useCase.execute(9999).map(r => assertEquals(r, Left(err)))
