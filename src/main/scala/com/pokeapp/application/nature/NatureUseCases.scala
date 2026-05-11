package com.pokeapp.application.nature

import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.Nature
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.ResourceRepository

class GetNatureUseCase[F[_]](repo: ResourceRepository[F, Nature]):
  def execute(id: Int): F[Either[DomainError, Nature]]            = repo.findById(id)
  def executeByName(name: String): F[Either[DomainError, Nature]] = repo.findByName(name)

class ListNatureUseCase[F[_]](repo: ResourceRepository[F, Nature]):
  def execute(
      limit: Int = 20,
      offset: Int = 0
  ): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    repo.list(limit, offset)
