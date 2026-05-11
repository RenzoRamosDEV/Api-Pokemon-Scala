package com.pokeapp.domain.port

import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}

// Puerto genérico para todos los recursos de PokeAPI distintos de Pokémon.
// `A` es el tipo del modelo de dominio (Ability, Berry, Move, etc.).
// Todas las implementaciones concretas viven en infrastructure/adapter.
trait ResourceRepository[F[_], A]:
  def findById(id: Int): F[Either[DomainError, A]]
  def findByName(name: String): F[Either[DomainError, A]]
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]]
