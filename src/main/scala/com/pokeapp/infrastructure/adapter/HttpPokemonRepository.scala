package com.pokeapp.infrastructure.adapter

import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.Pokemon
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.PokemonRepository
import com.pokeapp.infrastructure.http.client.PokeApiClient

// Implementación HTTP del puerto PokemonRepository.
// Traduce las llamadas del dominio a llamadas concretas del PokeApiClient.
// En producción esta clase queda envuelta por CachedPokemonRepository.
class HttpPokemonRepository[F[_]](client: PokeApiClient[F]) extends PokemonRepository[F]:
  def findById(id: Int): F[Either[DomainError, Pokemon]]        = client.getPokemon(id.toString)
  def findByName(name: String): F[Either[DomainError, Pokemon]] = client.getPokemon(name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listPokemon(limit, offset)
