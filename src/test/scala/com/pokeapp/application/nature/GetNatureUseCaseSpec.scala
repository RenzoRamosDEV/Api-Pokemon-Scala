package com.pokeapp.application.nature

import cats.effect.IO
import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.Nature
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.ResourceRepository
import munit.CatsEffectSuite

class GetNatureUseCaseSpec extends CatsEffectSuite:

  private val adamant = Nature(
    id = 3,
    name = "adamant",
    decreasedStat = Some(NamedResource("special-attack", "url")),
    increasedStat = Some(NamedResource("attack", "url")),
    hatesFlavor = Some(NamedResource("dry", "url")),
    likesFlavor = Some(NamedResource("spicy", "url")),
    statChanges = Nil,
    pokeathlonStatChanges = Nil
  )

  private def repo(result: Either[DomainError, Nature]): ResourceRepository[IO, Nature] =
    new ResourceRepository[IO, Nature]:
      def findById(id: Int): IO[Either[DomainError, Nature]]        = IO.pure(result)
      def findByName(name: String): IO[Either[DomainError, Nature]] = IO.pure(result)
      def list(limit: Int, offset: Int): IO[Either[DomainError, PaginatedResponse[NamedResource]]] =
        IO.pure(Right(PaginatedResponse(0, None, None, Nil)))

  test("execute returns nature with stat changes"):
    val useCase = GetNatureUseCase(repo(Right(adamant)))
    useCase
      .execute(3)
      .map: r =>
        assertEquals(r.map(_.name), Right("adamant"))
        assertEquals(r.map(_.increasedStat.map(_.name)), Right(Some("attack")))

  test("execute returns NotFound for unknown nature"):
    val err     = DomainError.NotFound("Nature 999 not found")
    val useCase = GetNatureUseCase(repo(Left(err)))
    useCase.execute(999).map(r => assertEquals(r, Left(err)))
