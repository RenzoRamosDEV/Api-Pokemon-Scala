package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

// Datos de la especie de un Pokémon, mapeado desde GET /pokemon-species/{id}.
// Contiene información que no está en el endpoint /pokemon (lore, cadena evolutiva, etc.).
// Comparte el mismo ID/nombre que su Pokémon correspondiente.
case class PokemonSpecies(
    id: Int,
    name: String,
    isBaby: Boolean,
    isLegendary: Boolean,
    isMythical: Boolean,
    captureRate: Int,            // De 0 a 255; a mayor valor, más fácil de capturar
    baseHappiness: Option[Int],  // Felicidad inicial al capturarlo. None en algunos Pokémon especiales
    genderRate: Int,             // -1 = sin género; 0-8 = proporción hembra (8 = siempre hembra)
    flavorTextEntries: List[FlavorTextEntry], // Textos del Pokédex en distintos idiomas y versiones
    evolutionChain: Option[EvolutionChainRef] // None solo si el Pokémon no tiene cadena evolutiva registrada
)

// Texto del Pokédex para una versión e idioma específicos
case class FlavorTextEntry(
    flavorText: String,
    language: NamedResource,
    version: NamedResource
)

// Referencia a la cadena evolutiva via URL completa de PokeAPI.
// Para obtener el detalle hay que hacer un GET a esta URL.
case class EvolutionChainRef(url: String)
