package com.pokeapp.application.ability

import cats.effect.IO
import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.Ability
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.ResourceRepository
import munit.CatsEffectSuite

class GetAbilityUseCaseSpec extends CatsEffectSuite:

  private def repo(result: Either[DomainError, Ability]): ResourceRepository[IO, Ability] =
    new ResourceRepository[IO, Ability]:
      def findById(id: Int): IO[Either[DomainError, Ability]]        = IO.pure(result)
      def findByName(name: String): IO[Either[DomainError, Ability]] = IO.pure(result)
      def list(limit: Int, offset: Int): IO[Either[DomainError, PaginatedResponse[NamedResource]]] =
        IO.pure(Right(PaginatedResponse(0, None, None, Nil)))

  test("execute returns ability when it exists"):
    val useCase = GetAbilityUseCase(repo(Right(AbilityFixtures.static)))
    useCase.execute(1).map(r => assertEquals(r, Right(AbilityFixtures.static)))

  test("execute returns NotFound for unknown ability"):
    val err     = DomainError.NotFound("Ability 9999 not found")
    val useCase = GetAbilityUseCase(repo(Left(err)))
    useCase.execute(9999).map(r => assertEquals(r, Left(err)))

  test("executeByName returns ability by name"):
    val useCase = GetAbilityUseCase(repo(Right(AbilityFixtures.static)))
    useCase.executeByName("stench").map(r => assertEquals(r.map(_.name), Right("stench")))
