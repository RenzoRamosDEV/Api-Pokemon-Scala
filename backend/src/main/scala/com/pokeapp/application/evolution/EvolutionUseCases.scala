package com.pokeapp.application.evolution

import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.EvolutionChain
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.ResourceRepository

class GetEvolutionChainUseCase[F[_]](repo: ResourceRepository[F, EvolutionChain]):
  def execute(id: Int): F[Either[DomainError, EvolutionChain]] = repo.findById(id)

class ListEvolutionChainUseCase[F[_]](repo: ResourceRepository[F, EvolutionChain]):
  def execute(
      limit: Int = 20,
      offset: Int = 0
  ): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    repo.list(limit, offset)
