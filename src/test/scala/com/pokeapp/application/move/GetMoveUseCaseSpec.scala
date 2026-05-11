package com.pokeapp.application.move

import cats.effect.IO
import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.Move
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.ResourceRepository
import munit.CatsEffectSuite

class GetMoveUseCaseSpec extends CatsEffectSuite:

  private val thunderbolt = Move(
    id = 85,
    name = "thunderbolt",
    accuracy = Some(100),
    pp = 15,
    priority = 0,
    power = Some(90),
    damageClass = NamedResource("special", "url"),
    moveType = NamedResource("electric", "url"),
    target = NamedResource("selected-pokemon", "url"),
    effectEntries = List(
      com.pokeapp.domain.model.MoveEffect(
        "Has 10% chance of paralyzing the target.",
        "10% chance of paralyzing the target.",
        NamedResource("en", "url")
      )
    ),
    meta = None
  )

  private def repo(result: Either[DomainError, Move]): ResourceRepository[IO, Move] =
    new ResourceRepository[IO, Move]:
      def findById(id: Int): IO[Either[DomainError, Move]]        = IO.pure(result)
      def findByName(name: String): IO[Either[DomainError, Move]] = IO.pure(result)
      def list(limit: Int, offset: Int): IO[Either[DomainError, PaginatedResponse[NamedResource]]] =
        IO.pure(Right(PaginatedResponse(0, None, None, Nil)))

  test("execute returns move when it exists"):
    val useCase = GetMoveUseCase(repo(Right(thunderbolt)))
    useCase.execute(85).map(r => assertEquals(r.map(_.name), Right("thunderbolt")))

  test("execute returns NotFound for unknown move"):
    val err     = DomainError.NotFound("Move 9999 not found")
    val useCase = GetMoveUseCase(repo(Left(err)))
    useCase.execute(9999).map(r => assertEquals(r, Left(err)))

  test("executeByName finds move by name"):
    val useCase = GetMoveUseCase(repo(Right(thunderbolt)))
    useCase.executeByName("thunderbolt").map(r => assertEquals(r.map(_.power), Right(Some(90))))
