package com.pokeapp.infrastructure.cache

import cats.effect.{Resource, Sync}
import com.github.blemale.scaffeine.{Cache, Scaffeine}

import scala.concurrent.duration.*

// Factory para construir instancias de caché Caffeine envueltas en un Resource[F].
// Caffeine es un caché en memoria JVM de alto rendimiento (sucesor de Guava Cache).
// Al ser un Resource, el ciclo de vida queda gestionado por Cats Effect.
object CaffeineCache:
  // Construye un caché con tamaño máximo y TTL configurables.
  // `maximumSize` aplica política LRU cuando se excede el límite.
  // `expireAfterWrite` expira entradas según su tiempo de escritura, no de lectura.
  def build[F[_]: Sync, K, V](maxSize: Long, ttl: FiniteDuration): Resource[F, Cache[K, V]] =
    Resource.eval(
      Sync[F].delay(
        Scaffeine()
          .maximumSize(maxSize)
          .expireAfterWrite(ttl)
          .build[K, V]()
      )
    )
