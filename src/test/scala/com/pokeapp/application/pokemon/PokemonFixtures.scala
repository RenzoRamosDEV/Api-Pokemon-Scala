package com.pokeapp.application.pokemon

import com.pokeapp.domain.model.*
import com.pokeapp.domain.model.shared.NamedResource

object PokemonFixtures:
  val pikachu: Pokemon = Pokemon(
    id = 25,
    name = "pikachu",
    baseExperience = Some(112),
    height = 4,
    weight = 60,
    isDefault = true,
    abilities = List(PokemonAbility(NamedResource("static", "url"), isHidden = false, slot = 1)),
    moves = List(PokemonMove(NamedResource("thunder-shock", "url"))),
    stats = List(PokemonStat(NamedResource("speed", "url"), baseStat = 90, effort = 2)),
    types = List(PokemonType(slot = 1, `type` = NamedResource("electric", "url"))),
    sprites = Sprites(Some("front.png"), None, None, None)
  )
