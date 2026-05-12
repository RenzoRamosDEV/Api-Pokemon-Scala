package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

// Habilidad de un Pokémon, mapeada desde el endpoint GET /ability/{id} de PokeAPI.
// `isMainSeries` es false en habilidades que solo aparecen en spin-offs.
case class Ability(
    id: Int,
    name: String,
    isMainSeries: Boolean,
    effectEntries: List[AbilityEffect], // Descripciones de efecto por idioma
    pokemon: List[AbilityPokemon]       // Lista de Pokémon que pueden tener esta habilidad
)

// Descripción del efecto de una habilidad en un idioma concreto
case class AbilityEffect(effect: String, shortEffect: String, language: NamedResource)

// Referencia a un Pokémon que posee esta habilidad, con su slot e indicador de habilidad oculta
case class AbilityPokemon(isHidden: Boolean, slot: Int, pokemon: NamedResource)
