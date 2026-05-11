package com.pokeapp.application.item

import cats.effect.IO
import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.{Item, ItemSprites}
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.ResourceRepository
import munit.CatsEffectSuite

class GetItemUseCaseSpec extends CatsEffectSuite:

  private val masterBall = Item(
    id = 1,
    name = "master-ball",
    cost = 0,
    flingPower = Some(10),
    flingEffect = None,
    attributes = List(NamedResource("holdable", "url")),
    category = NamedResource("standard-balls", "url"),
    effectEntries = Nil,
    sprites = ItemSprites(Some("master-ball.png"))
  )

  private def repo(result: Either[DomainError, Item]): ResourceRepository[IO, Item] =
    new ResourceRepository[IO, Item]:
      def findById(id: Int): IO[Either[DomainError, Item]]        = IO.pure(result)
      def findByName(name: String): IO[Either[DomainError, Item]] = IO.pure(result)
      def list(limit: Int, offset: Int): IO[Either[DomainError, PaginatedResponse[NamedResource]]] =
        IO.pure(Right(PaginatedResponse(0, None, None, Nil)))

  test("execute returns item when it exists"):
    val useCase = GetItemUseCase(repo(Right(masterBall)))
    useCase.execute(1).map(r => assertEquals(r.map(_.name), Right("master-ball")))

  test("execute returns NotFound for unknown item"):
    val err     = DomainError.NotFound("Item 9999 not found")
    val useCase = GetItemUseCase(repo(Left(err)))
    useCase.execute(9999).map(r => assertEquals(r, Left(err)))
