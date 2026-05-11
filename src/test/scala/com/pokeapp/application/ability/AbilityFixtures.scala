package com.pokeapp.application.ability

import com.pokeapp.domain.model.*
import com.pokeapp.domain.model.shared.NamedResource

object AbilityFixtures:
  val static: Ability = Ability(
    id = 1,
    name = "stench",
    isMainSeries = true,
    effectEntries = List(
      AbilityEffect("May cause flinching", "May cause flinching.", NamedResource("en", "url"))
    ),
    pokemon = List(AbilityPokemon(isHidden = false, slot = 3, NamedResource("gloom", "url")))
  )
