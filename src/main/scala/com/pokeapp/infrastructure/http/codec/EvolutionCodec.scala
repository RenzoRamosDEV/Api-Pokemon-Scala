package com.pokeapp.infrastructure.http.codec

import com.pokeapp.domain.model.*
import com.pokeapp.domain.model.shared.NamedResource
import com.pokeapp.infrastructure.http.codec.PokemonCodec.given
import io.circe.{Decoder, HCursor}

// Decoders para la cadena evolutiva de PokeAPI.
// ChainLink es recursivo (evolvesTo contiene más ChainLinks), por lo que
// el decoder de ChainLink usa Decoder.recursive para evitar stack overflow.
object EvolutionCodec:

  given Decoder[EvolutionDetail] = (c: HCursor) =>
    for
      trigger               <- c.downField("trigger").as[NamedResource]
      item                  <- c.downField("item").as[Option[NamedResource]]
      minLevel              <- c.downField("min_level").as[Option[Int]]
      minHappiness          <- c.downField("min_happiness").as[Option[Int]]
      minBeauty             <- c.downField("min_beauty").as[Option[Int]]
      minAffection          <- c.downField("min_affection").as[Option[Int]]
      needsOverworldRain    <- c.downField("needs_overworld_rain").as[Boolean]
      heldItem              <- c.downField("held_item").as[Option[NamedResource]]
      knownMove             <- c.downField("known_move").as[Option[NamedResource]]
      knownMoveType         <- c.downField("known_move_type").as[Option[NamedResource]]
      location              <- c.downField("location").as[Option[NamedResource]]
      partySpecies          <- c.downField("party_species").as[Option[NamedResource]]
      partyType             <- c.downField("party_type").as[Option[NamedResource]]
      relativePhysicalStats <- c.downField("relative_physical_stats").as[Option[Int]]
      timeOfDay             <- c.downField("time_of_day").as[String]
      tradeSpecies          <- c.downField("trade_species").as[Option[NamedResource]]
      turnUpsideDown        <- c.downField("turn_upside_down").as[Boolean]
    yield EvolutionDetail(trigger, item, minLevel, minHappiness, minBeauty, minAffection,
      needsOverworldRain, heldItem, knownMove, knownMoveType, location, partySpecies,
      partyType, relativePhysicalStats, timeOfDay, tradeSpecies, turnUpsideDown)

  // Decoder.recursive permite que el decoder de ChainLink se referencie a sí mismo
  // para decodificar la lista `evolves_to` de forma lazy (sin recursión infinita en la definición)
  given Decoder[ChainLink] = Decoder.recursive: recurse =>
    (c: HCursor) =>
      for
        isBaby           <- c.downField("is_baby").as[Boolean]
        species          <- c.downField("species").as[NamedResource]
        evolutionDetails <- c.downField("evolution_details").as[List[EvolutionDetail]]
        evolvesTo        <- c.downField("evolves_to").as[List[ChainLink]](using Decoder.decodeList(recurse))
      yield ChainLink(isBaby, species, evolutionDetails, evolvesTo)

  given Decoder[EvolutionChain] = (c: HCursor) =>
    for
      id              <- c.downField("id").as[Int]
      babyTriggerItem <- c.downField("baby_trigger_item").as[Option[NamedResource]]
      chain           <- c.downField("chain").as[ChainLink]
    yield EvolutionChain(id, babyTriggerItem, chain)
