package com.pokeapp.infrastructure.adapter

import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.*
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.ResourceRepository
import com.pokeapp.infrastructure.http.client.PokeApiClient
import com.pokeapp.infrastructure.http.codec.AbilityCodec.given
import com.pokeapp.infrastructure.http.codec.BerryCodec.given
import com.pokeapp.infrastructure.http.codec.EvolutionCodec.given
import com.pokeapp.infrastructure.http.codec.ItemCodec.given
import com.pokeapp.infrastructure.http.codec.WorldCodecs.given
import com.pokeapp.infrastructure.http.codec.MoveCodec.given
import com.pokeapp.infrastructure.http.codec.NatureCodec.given
import com.pokeapp.infrastructure.http.codec.TypeCodec.given

class HttpAbilityRepository[F[_]](client: PokeApiClient[F]) extends ResourceRepository[F, Ability]:
  def findById(id: Int): F[Either[DomainError, Ability]] =
    client.getResource[Ability]("ability", id.toString)
  def findByName(name: String): F[Either[DomainError, Ability]] =
    client.getResource[Ability]("ability", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("ability", limit, offset)

class HttpBerryRepository[F[_]](client: PokeApiClient[F]) extends ResourceRepository[F, Berry]:
  def findById(id: Int): F[Either[DomainError, Berry]] =
    client.getResource[Berry]("berry", id.toString)
  def findByName(name: String): F[Either[DomainError, Berry]] =
    client.getResource[Berry]("berry", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("berry", limit, offset)

class HttpMoveRepository[F[_]](client: PokeApiClient[F]) extends ResourceRepository[F, Move]:
  def findById(id: Int): F[Either[DomainError, Move]] =
    client.getResource[Move]("move", id.toString)
  def findByName(name: String): F[Either[DomainError, Move]] =
    client.getResource[Move]("move", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("move", limit, offset)

class HttpTypeRepository[F[_]](client: PokeApiClient[F]) extends ResourceRepository[F, Type]:
  def findById(id: Int): F[Either[DomainError, Type]] =
    client.getResource[Type]("type", id.toString)
  def findByName(name: String): F[Either[DomainError, Type]] =
    client.getResource[Type]("type", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("type", limit, offset)

class HttpItemRepository[F[_]](client: PokeApiClient[F]) extends ResourceRepository[F, Item]:
  def findById(id: Int): F[Either[DomainError, Item]] =
    client.getResource[Item]("item", id.toString)
  def findByName(name: String): F[Either[DomainError, Item]] =
    client.getResource[Item]("item", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("item", limit, offset)

class HttpNatureRepository[F[_]](client: PokeApiClient[F]) extends ResourceRepository[F, Nature]:
  def findById(id: Int): F[Either[DomainError, Nature]] =
    client.getResource[Nature]("nature", id.toString)
  def findByName(name: String): F[Either[DomainError, Nature]] =
    client.getResource[Nature]("nature", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("nature", limit, offset)

class HttpEvolutionRepository[F[_]](client: PokeApiClient[F])
    extends ResourceRepository[F, EvolutionChain]:
  def findById(id: Int): F[Either[DomainError, EvolutionChain]] =
    client.getResource[EvolutionChain]("evolution-chain", id.toString)
  def findByName(name: String): F[Either[DomainError, EvolutionChain]] =
    client.getResource[EvolutionChain]("evolution-chain", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("evolution-chain", limit, offset)

class HttpLocationRepository[F[_]](client: PokeApiClient[F])
    extends ResourceRepository[F, Location]:
  def findById(id: Int): F[Either[DomainError, Location]] =
    client.getResource[Location]("location", id.toString)
  def findByName(name: String): F[Either[DomainError, Location]] =
    client.getResource[Location]("location", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("location", limit, offset)

class HttpGenerationRepository[F[_]](client: PokeApiClient[F])
    extends ResourceRepository[F, Generation]:
  def findById(id: Int): F[Either[DomainError, Generation]] =
    client.getResource[Generation]("generation", id.toString)
  def findByName(name: String): F[Either[DomainError, Generation]] =
    client.getResource[Generation]("generation", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("generation", limit, offset)

class HttpRegionRepository[F[_]](client: PokeApiClient[F]) extends ResourceRepository[F, Region]:
  def findById(id: Int): F[Either[DomainError, Region]] =
    client.getResource[Region]("region", id.toString)
  def findByName(name: String): F[Either[DomainError, Region]] =
    client.getResource[Region]("region", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("region", limit, offset)

class HttpStatRepository[F[_]](client: PokeApiClient[F]) extends ResourceRepository[F, Stat]:
  def findById(id: Int): F[Either[DomainError, Stat]] =
    client.getResource[Stat]("stat", id.toString)
  def findByName(name: String): F[Either[DomainError, Stat]] =
    client.getResource[Stat]("stat", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("stat", limit, offset)

class HttpPokedexRepository[F[_]](client: PokeApiClient[F]) extends ResourceRepository[F, Pokedex]:
  def findById(id: Int): F[Either[DomainError, Pokedex]] =
    client.getResource[Pokedex]("pokedex", id.toString)
  def findByName(name: String): F[Either[DomainError, Pokedex]] =
    client.getResource[Pokedex]("pokedex", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("pokedex", limit, offset)
