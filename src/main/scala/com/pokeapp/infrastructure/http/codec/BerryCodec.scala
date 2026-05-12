package com.pokeapp.infrastructure.http.codec

import com.pokeapp.domain.model.*
import com.pokeapp.domain.model.shared.NamedResource
import com.pokeapp.infrastructure.http.codec.PokemonCodec.given
import io.circe.{Decoder, HCursor}

// Decoders Circe para deserializar la respuesta JSON de /berry/{id} al modelo de dominio.
object BerryCodec:

  // Deserializa un elemento del array "flavors" que indica la potencia de cada sabor
  given Decoder[BerryFlavorMap] = (c: HCursor) =>
    for
      potency <- c.downField("potency").as[Int]
      flavor  <- c.downField("flavor").as[NamedResource]
    yield BerryFlavorMap(potency, flavor)

  // Decoder raíz que une todos los campos del objeto berry de PokeAPI
  given Decoder[Berry] = (c: HCursor) =>
    for
      id               <- c.downField("id").as[Int]
      name             <- c.downField("name").as[String]
      growthTime       <- c.downField("growth_time").as[Int]
      maxHarvest       <- c.downField("max_harvest").as[Int]
      naturalGiftPower <- c.downField("natural_gift_power").as[Int]
      size             <- c.downField("size").as[Int]
      smoothness       <- c.downField("smoothness").as[Int]
      soilDryness      <- c.downField("soil_dryness").as[Int]
      firmness         <- c.downField("firmness").as[NamedResource]
      flavors          <- c.downField("flavors").as[List[BerryFlavorMap]]
      item             <- c.downField("item").as[NamedResource]
      naturalGiftType  <- c.downField("natural_gift_type").as[NamedResource]
    yield Berry(
      id,
      name,
      growthTime,
      maxHarvest,
      naturalGiftPower,
      size,
      smoothness,
      soilDryness,
      firmness,
      flavors,
      item,
      naturalGiftType
    )
