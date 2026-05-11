package com.pokeapp.application.`type`

import cats.effect.IO
import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.{Type, TypeDamageRelations}
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.ResourceRepository
import munit.CatsEffectSuite

class GetTypeUseCaseSpec extends CatsEffectSuite:

  private val electric = Type(
    id = 13,
    name = "electric",
    damageRelations = TypeDamageRelations(
      noDamageTo = List(NamedResource("ground", "url")),
      halfDamageTo = Nil,
      doubleDamageTo = List(NamedResource("water", "url"), NamedResource("flying", "url")),
      noDamageFrom = Nil,
      halfDamageFrom = List(
        NamedResource("electric", "url"),
        NamedResource("flying", "url"),
        NamedResource("steel", "url")
      ),
      doubleDamageFrom = List(NamedResource("ground", "url"))
    ),
    pokemon = Nil,
    moves = Nil
  )

  private def repo(result: Either[DomainError, Type]): ResourceRepository[IO, Type] =
    new ResourceRepository[IO, Type]:
      def findById(id: Int): IO[Either[DomainError, Type]]        = IO.pure(result)
      def findByName(name: String): IO[Either[DomainError, Type]] = IO.pure(result)
      def list(limit: Int, offset: Int): IO[Either[DomainError, PaginatedResponse[NamedResource]]] =
        IO.pure(Right(PaginatedResponse(0, None, None, Nil)))

  test("execute returns type with damage relations"):
    val useCase = GetTypeUseCase(repo(Right(electric)))
    useCase
      .execute(13)
      .map: r =>
        assertEquals(r.map(_.name), Right("electric"))
        assertEquals(r.map(_.damageRelations.doubleDamageTo.length), Right(2))

  test("execute returns NotFound for unknown type"):
    val err     = DomainError.NotFound("Type 999 not found")
    val useCase = GetTypeUseCase(repo(Left(err)))
    useCase.execute(999).map(r => assertEquals(r, Left(err)))
