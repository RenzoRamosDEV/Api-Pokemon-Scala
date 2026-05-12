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
import com.pokeapp.infrastructure.http.codec.MoveCodec.given
import com.pokeapp.infrastructure.http.codec.WorldCodecs.given
import com.pokeapp.infrastructure.http.codec.NatureCodec.given
import com.pokeapp.infrastructure.http.codec.TypeCodec.given

// Implementaciones HTTP del puerto ResourceRepository para cada tipo de recurso tipado.
// Cada clase delega directamente en PokeApiClient con el path de recurso correspondiente.
// Los decoders Circe necesarios se importan implícitamente desde los codecs de arriba.

// Repositorio HTTP para habilidades; delega en /ability/{id|name}
class HttpAbilityRepository[F[_]](client: PokeApiClient[F]) extends ResourceRepository[F, Ability]:
  def findById(id: Int): F[Either[DomainError, Ability]] =
    client.getResource[Ability]("ability", id.toString)
  def findByName(name: String): F[Either[DomainError, Ability]] =
    client.getResource[Ability]("ability", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("ability", limit, offset)

// Repositorio HTTP para bayas; delega en /berry/{id|name}
class HttpBerryRepository[F[_]](client: PokeApiClient[F]) extends ResourceRepository[F, Berry]:
  def findById(id: Int): F[Either[DomainError, Berry]] =
    client.getResource[Berry]("berry", id.toString)
  def findByName(name: String): F[Either[DomainError, Berry]] =
    client.getResource[Berry]("berry", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("berry", limit, offset)

// Repositorio HTTP para movimientos; delega en /move/{id|name}
class HttpMoveRepository[F[_]](client: PokeApiClient[F]) extends ResourceRepository[F, Move]:
  def findById(id: Int): F[Either[DomainError, Move]] =
    client.getResource[Move]("move", id.toString)
  def findByName(name: String): F[Either[DomainError, Move]] =
    client.getResource[Move]("move", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("move", limit, offset)

// Repositorio HTTP para tipos; delega en /type/{id|name}
class HttpTypeRepository[F[_]](client: PokeApiClient[F]) extends ResourceRepository[F, Type]:
  def findById(id: Int): F[Either[DomainError, Type]] =
    client.getResource[Type]("type", id.toString)
  def findByName(name: String): F[Either[DomainError, Type]] =
    client.getResource[Type]("type", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("type", limit, offset)

// Repositorio HTTP para items; delega en /item/{id|name}
class HttpItemRepository[F[_]](client: PokeApiClient[F]) extends ResourceRepository[F, Item]:
  def findById(id: Int): F[Either[DomainError, Item]] =
    client.getResource[Item]("item", id.toString)
  def findByName(name: String): F[Either[DomainError, Item]] =
    client.getResource[Item]("item", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("item", limit, offset)

// Repositorio HTTP para naturalezas; delega en /nature/{id|name}
class HttpNatureRepository[F[_]](client: PokeApiClient[F]) extends ResourceRepository[F, Nature]:
  def findById(id: Int): F[Either[DomainError, Nature]] =
    client.getResource[Nature]("nature", id.toString)
  def findByName(name: String): F[Either[DomainError, Nature]] =
    client.getResource[Nature]("nature", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("nature", limit, offset)

// Repositorio HTTP para cadenas evolutivas; delega en /evolution-chain/{id}
class HttpEvolutionRepository[F[_]](client: PokeApiClient[F])
    extends ResourceRepository[F, EvolutionChain]:
  def findById(id: Int): F[Either[DomainError, EvolutionChain]] =
    client.getResource[EvolutionChain]("evolution-chain", id.toString)
  def findByName(name: String): F[Either[DomainError, EvolutionChain]] =
    client.getResource[EvolutionChain]("evolution-chain", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("evolution-chain", limit, offset)

// Repositorio HTTP para localizaciones; delega en /location/{id|name}
class HttpLocationRepository[F[_]](client: PokeApiClient[F])
    extends ResourceRepository[F, Location]:
  def findById(id: Int): F[Either[DomainError, Location]] =
    client.getResource[Location]("location", id.toString)
  def findByName(name: String): F[Either[DomainError, Location]] =
    client.getResource[Location]("location", name)
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    client.listResource("location", limit, offset)

