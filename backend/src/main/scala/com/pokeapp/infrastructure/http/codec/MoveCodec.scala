package com.pokeapp.infrastructure.http.codec

import com.pokeapp.domain.model.*
import com.pokeapp.domain.model.shared.NamedResource
import com.pokeapp.infrastructure.http.codec.PokemonCodec.given
import io.circe.{Decoder, HCursor}

object MoveCodec:

  given Decoder[MoveEffect] = (c: HCursor) =>
    for
      effect      <- c.downField("effect").as[String]
      shortEffect <- c.downField("short_effect").as[String]
      language    <- c.downField("language").as[NamedResource]
    yield MoveEffect(effect, shortEffect, language)

  given Decoder[MoveMeta] = (c: HCursor) =>
    for
      ailment       <- c.downField("ailment").as[NamedResource]
      category      <- c.downField("category").as[NamedResource]
      minHits       <- c.downField("min_hits").as[Option[Int]]
      maxHits       <- c.downField("max_hits").as[Option[Int]]
      minTurns      <- c.downField("min_turns").as[Option[Int]]
      maxTurns      <- c.downField("max_turns").as[Option[Int]]
      drain         <- c.downField("drain").as[Int]
      healing       <- c.downField("healing").as[Int]
      critRate      <- c.downField("crit_rate").as[Int]
      ailmentChance <- c.downField("ailment_chance").as[Int]
      flinchChance  <- c.downField("flinch_chance").as[Int]
      statChance    <- c.downField("stat_chance").as[Int]
    yield MoveMeta(
      ailment,
      category,
      minHits,
      maxHits,
      minTurns,
      maxTurns,
      drain,
      healing,
      critRate,
      ailmentChance,
      flinchChance,
      statChance
    )

  given Decoder[Move] = (c: HCursor) =>
    for
      id            <- c.downField("id").as[Int]
      name          <- c.downField("name").as[String]
      accuracy      <- c.downField("accuracy").as[Option[Int]]
      pp            <- c.downField("pp").as[Int]
      priority      <- c.downField("priority").as[Int]
      power         <- c.downField("power").as[Option[Int]]
      damageClass   <- c.downField("damage_class").as[NamedResource]
      moveType      <- c.downField("type").as[NamedResource]
      target        <- c.downField("target").as[NamedResource]
      effectEntries <- c.downField("effect_entries").as[List[MoveEffect]]
      meta          <- c.downField("meta").as[Option[MoveMeta]]
    yield Move(
      id,
      name,
      accuracy,
      pp,
      priority,
      power,
      damageClass,
      moveType,
      target,
      effectEntries,
      meta
    )
