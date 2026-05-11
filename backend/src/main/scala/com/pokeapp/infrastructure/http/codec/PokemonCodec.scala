package com.pokeapp.infrastructure.http.codec

import com.pokeapp.domain.model.*
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import io.circe.{Decoder, HCursor}
import io.circe.generic.semiauto.*

// Decoders Circe para deserializar las respuestas JSON de PokeAPI al modelo de dominio Pokemon.
// Los campos de PokeAPI están en snake_case; por eso se usa HCursor con downField en lugar de deriveDecoder.
// Este objeto es importado por PokeApiClient y por los tests que construyen respuestas JSON mock.
object PokemonCodec:

  // NamedResource aparece en decenas de campos (ability.ability, move.move, stat.stat, etc.)
  // Se define aquí porque es base para los demás decoders de este archivo.
  given Decoder[NamedResource] = deriveDecoder[NamedResource]

  given Decoder[Sprites] = (c: HCursor) =>
    for
      frontDefault <- c.downField("front_default").as[Option[String]]
      frontShiny   <- c.downField("front_shiny").as[Option[String]]
      backDefault  <- c.downField("back_default").as[Option[String]]
      backShiny    <- c.downField("back_shiny").as[Option[String]]
    yield Sprites(frontDefault, frontShiny, backDefault, backShiny)

  given Decoder[PokemonAbility] = (c: HCursor) =>
    for
      ability  <- c.downField("ability").as[NamedResource]
      isHidden <- c.downField("is_hidden").as[Boolean]
      slot     <- c.downField("slot").as[Int]
    yield PokemonAbility(ability, isHidden, slot)

  given Decoder[PokemonMove] = (c: HCursor) =>
    c.downField("move").as[NamedResource].map(PokemonMove.apply)

  given Decoder[PokemonStat] = (c: HCursor) =>
    for
      stat     <- c.downField("stat").as[NamedResource]
      baseStat <- c.downField("base_stat").as[Int]
      effort   <- c.downField("effort").as[Int]
    yield PokemonStat(stat, baseStat, effort)

  given Decoder[PokemonType] = (c: HCursor) =>
    for
      slot     <- c.downField("slot").as[Int]
      typeName <- c.downField("type").as[NamedResource]
    yield PokemonType(slot, typeName)

  given Decoder[Pokemon] = (c: HCursor) =>
    for
      id             <- c.downField("id").as[Int]
      name           <- c.downField("name").as[String]
      baseExperience <- c.downField("base_experience").as[Option[Int]]
      height         <- c.downField("height").as[Int]
      weight         <- c.downField("weight").as[Int]
      isDefault      <- c.downField("is_default").as[Boolean]
      abilities      <- c.downField("abilities").as[List[PokemonAbility]]
      moves          <- c.downField("moves").as[List[PokemonMove]]
      stats          <- c.downField("stats").as[List[PokemonStat]]
      types          <- c.downField("types").as[List[PokemonType]]
      sprites        <- c.downField("sprites").as[Sprites]
    yield Pokemon(id, name, baseExperience, height, weight, isDefault, abilities, moves, stats, types, sprites)

  // Decoder genérico para todas las respuestas paginadas de PokeAPI
  given [A: Decoder]: Decoder[PaginatedResponse[A]] = (c: HCursor) =>
    for
      count    <- c.downField("count").as[Int]
      next     <- c.downField("next").as[Option[String]]
      previous <- c.downField("previous").as[Option[String]]
      results  <- c.downField("results").as[List[A]]
    yield PaginatedResponse(count, next, previous, results)
