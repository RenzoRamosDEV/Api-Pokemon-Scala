package com.pokeapp.application.`type`

import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.Type
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.ResourceRepository

// Obtiene un tipo elemental por ID o por nombre desde PokeAPI
class GetTypeUseCase[F[_]](repo: ResourceRepository[F, Type]):
  def execute(id: Int): F[Either[DomainError, Type]]            = repo.findById(id)
  def executeByName(name: String): F[Either[DomainError, Type]] = repo.findByName(name)

// Retorna una página de tipos (solo nombre y URL, sin detalle)
class ListTypeUseCase[F[_]](repo: ResourceRepository[F, Type]):
  def execute(
      limit: Int = 20,
      offset: Int = 0
  ): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    repo.list(limit, offset)
