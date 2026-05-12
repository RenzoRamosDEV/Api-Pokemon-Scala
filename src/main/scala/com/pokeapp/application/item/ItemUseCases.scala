package com.pokeapp.application.item

import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.Item
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.ResourceRepository

// Obtiene un item por ID o por nombre desde PokeAPI
class GetItemUseCase[F[_]](repo: ResourceRepository[F, Item]):
  def execute(id: Int): F[Either[DomainError, Item]]            = repo.findById(id)
  def executeByName(name: String): F[Either[DomainError, Item]] = repo.findByName(name)

// Retorna una página de items (solo nombre y URL, sin detalle)
class ListItemUseCase[F[_]](repo: ResourceRepository[F, Item]):
  def execute(
      limit: Int = 20,
      offset: Int = 0
  ): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    repo.list(limit, offset)
