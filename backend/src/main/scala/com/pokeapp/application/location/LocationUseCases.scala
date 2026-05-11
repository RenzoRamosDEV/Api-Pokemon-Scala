package com.pokeapp.application.location

import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.Location
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.ResourceRepository

class GetLocationUseCase[F[_]](repo: ResourceRepository[F, Location]):
  def execute(id: Int): F[Either[DomainError, Location]]            = repo.findById(id)
  def executeByName(name: String): F[Either[DomainError, Location]] = repo.findByName(name)

class ListLocationUseCase[F[_]](repo: ResourceRepository[F, Location]):
  def execute(
      limit: Int = 20,
      offset: Int = 0
  ): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    repo.list(limit, offset)
