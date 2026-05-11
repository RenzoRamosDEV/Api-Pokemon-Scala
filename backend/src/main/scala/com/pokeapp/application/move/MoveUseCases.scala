package com.pokeapp.application.move

import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.Move
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.ResourceRepository

class GetMoveUseCase[F[_]](repo: ResourceRepository[F, Move]):
  def execute(id: Int): F[Either[DomainError, Move]]            = repo.findById(id)
  def executeByName(name: String): F[Either[DomainError, Move]] = repo.findByName(name)

class ListMoveUseCase[F[_]](repo: ResourceRepository[F, Move]):
  def execute(
      limit: Int = 20,
      offset: Int = 0
  ): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    repo.list(limit, offset)
