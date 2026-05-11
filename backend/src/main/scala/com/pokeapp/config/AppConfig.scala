package com.pokeapp.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

// PureConfig lee automáticamente estos valores desde application.conf usando `derives ConfigReader`.
// Los nombres de los campos se mapean de camelCase (Scala) a kebab-case (HOCON): timeoutSeconds → timeout-seconds

// Configuración del servidor HTTP (host y puerto de escucha)
case class ServerConfig(host: String, port: Int) derives ConfigReader

// Configuración del cliente HTTP hacia PokeAPI
case class PokeApiConfig(
    baseUrl: String,
    timeoutSeconds: Int,
    maxRetries: Int
) derives ConfigReader

// Configuración del caché en memoria (Caffeine)
case class CacheConfig(
    maxSize: Int,    // Máximo de entradas antes de desalojar las menos usadas
    ttlMinutes: Int  // Tiempo de vida de cada entrada después de escribirse
) derives ConfigReader

// Configuración raíz que agrupa todas las secciones de application.conf
case class AppConfig(
    server: ServerConfig,
    pokeapi: PokeApiConfig,
    cache: CacheConfig
) derives ConfigReader
