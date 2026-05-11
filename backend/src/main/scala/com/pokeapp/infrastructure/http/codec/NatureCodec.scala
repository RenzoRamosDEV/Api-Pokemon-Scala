package com.pokeapp.infrastructure.http.codec

import com.pokeapp.domain.model.*
import com.pokeapp.domain.model.shared.NamedResource
import com.pokeapp.infrastructure.http.codec.PokemonCodec.given
import io.circe.{Decoder, HCursor}

object NatureCodec:

  given Decoder[NatureStatChange] = (c: HCursor) =>
    for
      maxChange <- c.downField("max_change").as[Int]
      stat      <- c.downField("stat").as[NamedResource]
    yield NatureStatChange(maxChange, stat)

  given Decoder[NaturePokeathlonStatChange] = (c: HCursor) =>
    for
      maxChange <- c.downField("max_change").as[Int]
      stat      <- c.downField("pokeathlon_stat").as[NamedResource]
    yield NaturePokeathlonStatChange(maxChange, stat)

  given Decoder[Nature] = (c: HCursor) =>
    for
      id            <- c.downField("id").as[Int]
      name          <- c.downField("name").as[String]
      decreasedStat <- c.downField("decreased_stat").as[Option[NamedResource]]
      increasedStat <- c.downField("increased_stat").as[Option[NamedResource]]
      hatesFlavor   <- c.downField("hates_flavor").as[Option[NamedResource]]
      likesFlavor   <- c.downField("likes_flavor").as[Option[NamedResource]]
      statChanges <- c
        .downField("move_battle_style_preferences")
        .as[Option[List[NatureStatChange]]]
        .map(_.getOrElse(Nil))
      pokeathlonStatChanges <- c
        .downField("pokeathlon_stat_changes")
        .as[Option[List[NaturePokeathlonStatChange]]]
        .map(_.getOrElse(Nil))
    yield Nature(
      id,
      name,
      decreasedStat,
      increasedStat,
      hatesFlavor,
      likesFlavor,
      statChanges,
      pokeathlonStatChanges
    )
