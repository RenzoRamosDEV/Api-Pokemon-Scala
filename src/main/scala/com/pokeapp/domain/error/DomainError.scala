package com.pokeapp.domain.error

// Errores de dominio que pueden ocurrir al operar con los recursos de PokeAPI.
// Al ser un enum sellado, el compilador fuerza el manejo exhaustivo en cada `match`.
enum DomainError:
  // El recurso solicitado no existe en PokeAPI (HTTP 404)
  case NotFound(message: String)

  // PokeAPI respondió con un status inesperado (distinto de 200/404/429)
  case ExternalApiError(message: String, statusCode: Int)

  // El JSON recibido no pudo deserializarse al modelo esperado
  case ParseError(message: String)

  // PokeAPI aplicó rate limiting (HTTP 429)
  case RateLimitExceeded
