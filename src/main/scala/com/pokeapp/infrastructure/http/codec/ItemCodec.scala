package com.pokeapp.infrastructure.http.codec

import com.pokeapp.domain.model.*
import com.pokeapp.domain.model.shared.NamedResource
import com.pokeapp.infrastructure.http.codec.PokemonCodec.given
import io.circe.{Decoder, HCursor}

// Decoders Circe para deserializar la respuesta JSON de /item/{id} al modelo de dominio.
object ItemCodec:

  // Deserializa la descripción del efecto del item en un idioma del array "effect_entries"
  given Decoder[ItemEffect] = (c: HCursor) =>
    for
      effect      <- c.downField("effect").as[String]
      shortEffect <- c.downField("short_effect").as[String]
      language    <- c.downField("language").as[NamedResource]
    yield ItemEffect(effect, shortEffect, language)

  // Deserializa el objeto "sprites" del item; el campo se llama "default" en la API
  given Decoder[ItemSprites] = (c: HCursor) =>
    c.downField("default").as[Option[String]].map(ItemSprites.apply)

  // Decoder raíz que une todos los campos del objeto item de PokeAPI
  given Decoder[Item] = (c: HCursor) =>
    for
      id            <- c.downField("id").as[Int]
      name          <- c.downField("name").as[String]
      cost          <- c.downField("cost").as[Int]
      flingPower    <- c.downField("fling_power").as[Option[Int]]
      flingEffect   <- c.downField("fling_effect").as[Option[NamedResource]]
      attributes    <- c.downField("attributes").as[List[NamedResource]]
      category      <- c.downField("category").as[NamedResource]
      effectEntries <- c.downField("effect_entries").as[List[ItemEffect]]
      sprites       <- c.downField("sprites").as[ItemSprites]
    yield Item(
      id,
      name,
      cost,
      flingPower,
      flingEffect,
      attributes,
      category,
      effectEntries,
      sprites
    )
