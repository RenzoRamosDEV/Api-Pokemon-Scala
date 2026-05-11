package com.pokeapp.infrastructure.http.codec

import com.pokeapp.domain.model.*
import com.pokeapp.domain.model.shared.NamedResource
import com.pokeapp.infrastructure.http.codec.PokemonCodec.given
import io.circe.{Decoder, HCursor}

object AbilityCodec:

  given Decoder[AbilityEffect] = (c: HCursor) =>
    for
      effect      <- c.downField("effect").as[String]
      shortEffect <- c.downField("short_effect").as[String]
      language    <- c.downField("language").as[NamedResource]
    yield AbilityEffect(effect, shortEffect, language)

  given Decoder[AbilityPokemon] = (c: HCursor) =>
    for
      isHidden <- c.downField("is_hidden").as[Boolean]
      slot     <- c.downField("slot").as[Int]
      pokemon  <- c.downField("pokemon").as[NamedResource]
    yield AbilityPokemon(isHidden, slot, pokemon)

  given Decoder[Ability] = (c: HCursor) =>
    for
      id            <- c.downField("id").as[Int]
      name          <- c.downField("name").as[String]
      isMainSeries  <- c.downField("is_main_series").as[Boolean]
      effectEntries <- c.downField("effect_entries").as[List[AbilityEffect]]
      pokemon       <- c.downField("pokemon").as[List[AbilityPokemon]]
    yield Ability(id, name, isMainSeries, effectEntries, pokemon)
