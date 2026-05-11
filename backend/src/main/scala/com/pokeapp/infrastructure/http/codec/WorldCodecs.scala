package com.pokeapp.infrastructure.http.codec

import com.pokeapp.domain.model.*
import com.pokeapp.domain.model.shared.NamedResource
import com.pokeapp.infrastructure.http.codec.PokemonCodec.given
import io.circe.{Decoder, HCursor}

// Decoders para recursos de "mundo" de PokeAPI: Location, Region, Generation, Stat y Pokedex.
// Estos comparten archivo porque son recursos de contexto/meta que rara vez cambian.
object WorldCodecs:

  given Decoder[Location] = (c: HCursor) =>
    for
      id     <- c.downField("id").as[Int]
      name   <- c.downField("name").as[String]
      region <- c.downField("region").as[Option[NamedResource]] // None si la location no pertenece a una región
      areas  <- c.downField("areas").as[List[NamedResource]]
    yield Location(id, name, region, areas)

  given Decoder[Generation] = (c: HCursor) =>
    for
      id             <- c.downField("id").as[Int]
      name           <- c.downField("name").as[String]
      abilities      <- c.downField("abilities").as[List[NamedResource]]
      moves          <- c.downField("moves").as[List[NamedResource]]
      pokemonSpecies <- c.downField("pokemon_species").as[List[NamedResource]]
      types          <- c.downField("types").as[List[NamedResource]]
      versionGroups  <- c.downField("version_groups").as[List[NamedResource]]
      mainRegion     <- c.downField("main_region").as[NamedResource]
    yield Generation(id, name, abilities, moves, pokemonSpecies, types, versionGroups, mainRegion)

  given Decoder[Region] = (c: HCursor) =>
    for
      id             <- c.downField("id").as[Int]
      name           <- c.downField("name").as[String]
      locations      <- c.downField("locations").as[List[NamedResource]]
      mainGeneration <- c.downField("main_generation").as[Option[NamedResource]]
      pokedexes      <- c.downField("pokedexes").as[List[NamedResource]]
      versionGroups  <- c.downField("version_groups").as[List[NamedResource]]
    yield Region(id, name, locations, mainGeneration, pokedexes, versionGroups)

  // ── Stat ─────────────────────────────────────────────────────────────────

  given Decoder[MoveStatAffect] = (c: HCursor) =>
    for
      change <- c.downField("change").as[Int]
      move   <- c.downField("move").as[NamedResource]
    yield MoveStatAffect(change, move)

  given Decoder[StatAffectingMoves] = (c: HCursor) =>
    for
      increase <- c.downField("increase").as[List[MoveStatAffect]]
      decrease <- c.downField("decrease").as[List[MoveStatAffect]]
    yield StatAffectingMoves(increase, decrease)

  given Decoder[StatAffectingNatures] = (c: HCursor) =>
    for
      increase <- c.downField("increase").as[List[NamedResource]]
      decrease <- c.downField("decrease").as[List[NamedResource]]
    yield StatAffectingNatures(increase, decrease)

  given Decoder[Stat] = (c: HCursor) =>
    for
      id               <- c.downField("id").as[Int]
      name             <- c.downField("name").as[String]
      gameIndex        <- c.downField("game_index").as[Int]
      isBattleOnly     <- c.downField("is_battle_only").as[Boolean]
      affectingMoves   <- c.downField("affecting_moves").as[StatAffectingMoves]
      affectingNatures <- c.downField("affecting_natures").as[StatAffectingNatures]
    yield Stat(id, name, gameIndex, isBattleOnly, affectingMoves, affectingNatures)

  // ── Pokedex ───────────────────────────────────────────────────────────────

  given Decoder[PokemonEntry] = (c: HCursor) =>
    for
      entryNumber    <- c.downField("entry_number").as[Int]
      pokemonSpecies <- c.downField("pokemon_species").as[NamedResource]
    yield PokemonEntry(entryNumber, pokemonSpecies)

  given Decoder[Pokedex] = (c: HCursor) =>
    for
      id             <- c.downField("id").as[Int]
      name           <- c.downField("name").as[String]
      isMainSeries   <- c.downField("is_main_series").as[Boolean]
      region         <- c.downField("region").as[Option[NamedResource]]
      pokemonEntries <- c.downField("pokemon_entries").as[List[PokemonEntry]]
    yield Pokedex(id, name, isMainSeries, region, pokemonEntries)
