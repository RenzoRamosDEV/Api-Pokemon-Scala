package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

case class Ability(
    id: Int,
    name: String,
    isMainSeries: Boolean,
    effectEntries: List[AbilityEffect],
    pokemon: List[AbilityPokemon]
)

case class AbilityEffect(effect: String, shortEffect: String, language: NamedResource)

case class AbilityPokemon(isHidden: Boolean, slot: Int, pokemon: NamedResource)
