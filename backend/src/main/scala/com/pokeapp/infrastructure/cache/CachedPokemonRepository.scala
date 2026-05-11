package com.pokeapp.infrastructure.cache

import cats.effect.Sync
import cats.syntax.all.*
import com.github.blemale.scaffeine.Cache
import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.Pokemon
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.PokemonRepository

// Decorador de PokemonRepository que añade caché en memoria para búsquedas individuales.
// Implementa el patrón Cache-Aside: primero busca en caché y si no está, consulta el repo subyacente.
// Las listas paginadas no se cachean porque varían por parámetros y consumen más memoria.
class CachedPokemonRepository[F[_]: Sync](
    underlying: PokemonRepository[F],
    cache: Cache[String, Pokemon]
) extends PokemonRepository[F]:

  // Comprueba el caché con `key`. Si hay hit, devuelve inmediatamente.
  // Si hay miss, llama a `fetch`, y solo guarda en caché si el resultado es Right
  // (no cacheamos errores para que reintentos futuros puedan recuperarse).
  private def cachedLookup(
      key: String,
      fetch: F[Either[DomainError, Pokemon]]
  ): F[Either[DomainError, Pokemon]] =
    cache.getIfPresent(key) match
      case Some(pokemon) => pokemon.asRight[DomainError].pure[F]
      case None =>
        fetch.flatTap:
          case Right(pokemon) => Sync[F].delay(cache.put(key, pokemon))
          case Left(_)        => Sync[F].unit

  // Las claves usan prefijo para evitar colisiones entre búsquedas por ID y por nombre.
  // Ej: "id:25" y "name:pikachu" pueden coexistir y ambas apuntan a Pikachu.
  def findById(id: Int): F[Either[DomainError, Pokemon]] =
    cachedLookup(s"id:$id", underlying.findById(id))

  def findByName(name: String): F[Either[DomainError, Pokemon]] =
    cachedLookup(s"name:$name", underlying.findByName(name))

  // Las listas se delegan directamente sin caché
  def list(limit: Int, offset: Int): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    underlying.list(limit, offset)
