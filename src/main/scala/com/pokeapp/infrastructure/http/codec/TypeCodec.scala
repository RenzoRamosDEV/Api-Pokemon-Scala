package com.pokeapp.infrastructure.http.codec

import com.pokeapp.domain.model.*
import com.pokeapp.domain.model.shared.NamedResource
import com.pokeapp.infrastructure.http.codec.PokemonCodec.given
import io.circe.{Decoder, HCursor}

// Decoders Circe para deserializar la respuesta JSON de /type/{id} al modelo de dominio.
object TypeCodec:

  // Deserializa un elemento del array "pokemon" que lista Pokémon de este tipo
  given Decoder[TypePokemon] = (c: HCursor) =>
    for
      slot    <- c.downField("slot").as[Int]
      pokemon <- c.downField("pokemon").as[NamedResource]
    yield TypePokemon(slot, pokemon)

  // Deserializa el objeto "damage_relations" con las seis listas de efectividades
  given Decoder[TypeDamageRelations] = (c: HCursor) =>
    for
      noDamageTo       <- c.downField("no_damage_to").as[List[NamedResource]]
      halfDamageTo     <- c.downField("half_damage_to").as[List[NamedResource]]
      doubleDamageTo   <- c.downField("double_damage_to").as[List[NamedResource]]
      noDamageFrom     <- c.downField("no_damage_from").as[List[NamedResource]]
      halfDamageFrom   <- c.downField("half_damage_from").as[List[NamedResource]]
      doubleDamageFrom <- c.downField("double_damage_from").as[List[NamedResource]]
    yield TypeDamageRelations(
      noDamageTo,
      halfDamageTo,
      doubleDamageTo,
      noDamageFrom,
      halfDamageFrom,
      doubleDamageFrom
    )

  // Decoder raíz que une todos los campos del objeto type de PokeAPI
  given Decoder[Type] = (c: HCursor) =>
    for
      id              <- c.downField("id").as[Int]
      name            <- c.downField("name").as[String]
      damageRelations <- c.downField("damage_relations").as[TypeDamageRelations]
      pokemon         <- c.downField("pokemon").as[List[TypePokemon]]
      moves           <- c.downField("moves").as[List[NamedResource]]
    yield Type(id, name, damageRelations, pokemon, moves)
