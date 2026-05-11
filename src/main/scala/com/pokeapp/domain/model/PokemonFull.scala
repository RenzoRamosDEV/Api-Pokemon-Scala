package com.pokeapp.domain.model

// Respuesta enriquecida que combina tres llamadas a PokeAPI en un único objeto.
// Sirve al endpoint GET /api/v1/pokemon/{id}/full para que el frontend no necesite
// hacer múltiples requests para obtener datos del Pokémon, su especie y su evolución.
// `evolutionChain` es Option porque si la llamada a PokeAPI falla no bloqueamos
// toda la respuesta; se devuelve igual con None en lugar de devolver error.
case class PokemonFull(
    pokemon: Pokemon,
    species: PokemonSpecies,
    evolutionChain: Option[EvolutionChain]
)
