package com.pokeapp.application.berry

import cats.effect.IO
import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.Berry
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.ResourceRepository
import munit.CatsEffectSuite

class GetBerryUseCaseSpec extends CatsEffectSuite:

  private val cheri = Berry(
    id = 1,
    name = "cheri",
    growthTime = 3,
    maxHarvest = 5,
    naturalGiftPower = 60,
    size = 20,
    smoothness = 25,
    soilDryness = 15,
    firmness = NamedResource("soft", "url"),
    flavors = Nil,
    item = NamedResource("cheri-berry", "url"),
    naturalGiftType = NamedResource("fire", "url")
  )

  private def repo(result: Either[DomainError, Berry]): ResourceRepository[IO, Berry] =
    new ResourceRepository[IO, Berry]:
      def findById(id: Int): IO[Either[DomainError, Berry]]        = IO.pure(result)
      def findByName(name: String): IO[Either[DomainError, Berry]] = IO.pure(result)
      def list(limit: Int, offset: Int): IO[Either[DomainError, PaginatedResponse[NamedResource]]] =
        IO.pure(Right(PaginatedResponse(0, None, None, Nil)))

  test("execute returns berry when it exists"):
    val useCase = GetBerryUseCase(repo(Right(cheri)))
    useCase.execute(1).map(r => assertEquals(r.map(_.name), Right("cheri")))

  test("execute returns NotFound for unknown berry"):
    val err     = DomainError.NotFound("Berry 9999 not found")
    val useCase = GetBerryUseCase(repo(Left(err)))
    useCase.execute(9999).map(r => assertEquals(r, Left(err)))
