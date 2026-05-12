package com.pokeapp.application.ability

import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.Ability
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.ResourceRepository

// Obtiene una habilidad individual por ID o por nombre desde PokeAPI
class GetAbilityUseCase[F[_]](repo: ResourceRepository[F, Ability]):
  def execute(id: Int): F[Either[DomainError, Ability]]            = repo.findById(id)
  def executeByName(name: String): F[Either[DomainError, Ability]] = repo.findByName(name)

// Retorna una página de habilidades (solo nombre y URL, sin detalle)
class ListAbilityUseCase[F[_]](repo: ResourceRepository[F, Ability]):
  def execute(
      limit: Int = 20,
      offset: Int = 0
  ): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    repo.list(limit, offset)
