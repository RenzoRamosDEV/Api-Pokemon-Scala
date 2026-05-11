package com.pokeapp.domain.port

import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.Pokemon
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}

// Puerto de dominio para acceder a Pokémon.
// Define el contrato que deben cumplir todas las implementaciones (HTTP, cache, mock en tests).
// El tipo `F[_]` permite que sea agnóstico al efecto (IO, Future, etc.).
trait PokemonRepository[F[_]]:
  def findById(id: Int): F[Either[DomainError, Pokemon]]
  def findByName(name: String): F[Either[DomainError, Pokemon]]
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]]
