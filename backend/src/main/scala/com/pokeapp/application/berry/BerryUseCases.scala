package com.pokeapp.application.berry

import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.Berry
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.ResourceRepository

class GetBerryUseCase[F[_]](repo: ResourceRepository[F, Berry]):
  def execute(id: Int): F[Either[DomainError, Berry]]            = repo.findById(id)
  def executeByName(name: String): F[Either[DomainError, Berry]] = repo.findByName(name)

class ListBerryUseCase[F[_]](repo: ResourceRepository[F, Berry]):
  def execute(
      limit: Int = 20,
      offset: Int = 0
  ): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    repo.list(limit, offset)
