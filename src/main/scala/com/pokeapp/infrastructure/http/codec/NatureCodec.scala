package com.pokeapp.infrastructure.http.codec

import com.pokeapp.domain.model.*
import com.pokeapp.domain.model.shared.NamedResource
import com.pokeapp.infrastructure.http.codec.PokemonCodec.given
import io.circe.{Decoder, HCursor}

// Decoders Circe para deserializar la respuesta JSON de /nature/{id} al modelo de dominio.
object NatureCodec:

  // Deserializa un cambio de estadística principal del array "stat_changes"
  given Decoder[NatureStatChange] = (c: HCursor) =>
    for
      maxChange <- c.downField("max_change").as[Int]
      stat      <- c.downField("stat").as[NamedResource]
    yield NatureStatChange(maxChange, stat)

  // Deserializa un cambio de estadística del Pokéatlón del array "pokeathlon_stat_changes"
  given Decoder[NaturePokeathlonStatChange] = (c: HCursor) =>
    for
      maxChange <- c.downField("max_change").as[Int]
      stat      <- c.downField("pokeathlon_stat").as[NamedResource]
    yield NaturePokeathlonStatChange(maxChange, stat)

  // Decoder raíz que une todos los campos del objeto nature de PokeAPI.
  // stat_changes y pokeathlon_stat_changes se leen como Option[List] para tolerar arrays vacíos
  // que en algunas versiones de PokeAPI llegan como null en lugar de [].
  given Decoder[Nature] = (c: HCursor) =>
    for
      id            <- c.downField("id").as[Int]
      name          <- c.downField("name").as[String]
      decreasedStat <- c.downField("decreased_stat").as[Option[NamedResource]]
      increasedStat <- c.downField("increased_stat").as[Option[NamedResource]]
      hatesFlavor   <- c.downField("hates_flavor").as[Option[NamedResource]]
      likesFlavor   <- c.downField("likes_flavor").as[Option[NamedResource]]
      statChanges <- c
        .downField("stat_changes")
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
