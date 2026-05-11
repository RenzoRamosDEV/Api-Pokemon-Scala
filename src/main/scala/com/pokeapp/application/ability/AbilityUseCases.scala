package com.pokeapp.application.ability

import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.Ability
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.ResourceRepository

class GetAbilityUseCase[F[_]](repo: ResourceRepository[F, Ability]):
  def execute(id: Int): F[Either[DomainError, Ability]]            = repo.findById(id)
  def executeByName(name: String): F[Either[DomainError, Ability]] = repo.findByName(name)

class ListAbilityUseCase[F[_]](repo: ResourceRepository[F, Ability]):
  def execute(
      limit: Int = 20,
      offset: Int = 0
  ): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    repo.list(limit, offset)
