package com.pokeapp.application.location

import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.Location
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.ResourceRepository

// Obtiene una localización por ID o por nombre desde PokeAPI
class GetLocationUseCase[F[_]](repo: ResourceRepository[F, Location]):
  def execute(id: Int): F[Either[DomainError, Location]]            = repo.findById(id)
  def executeByName(name: String): F[Either[DomainError, Location]] = repo.findByName(name)

// Retorna una página de localizaciones (solo nombre y URL, sin detalle)
class ListLocationUseCase[F[_]](repo: ResourceRepository[F, Location]):
  def execute(
      limit: Int = 20,
      offset: Int = 0
  ): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    repo.list(limit, offset)
