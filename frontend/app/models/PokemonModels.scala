package models

import play.api.libs.json.*
import play.api.libs.functional.syntax.*

case class NamedResource(name: String, url: String)

case class Sprites(frontDefault: Option[String])

case class PokemonType(slot: Int, typeName: String)

case class PokemonAbility(name: String, isHidden: Boolean, slot: Int)

case class PokemonStat(name: String, baseStat: Int, effort: Int)

case class Pokemon(
    id: Int,
    name: String,
    baseExperience: Option[Int],
    height: Int,
    weight: Int,
    abilities: List[PokemonAbility],
    stats: List[PokemonStat],
    types: List[PokemonType],
    sprites: Sprites
)

case class PaginatedResponse(count: Int, results: List[NamedResource])

object PokemonModels:

  given Reads[NamedResource] = Json.reads[NamedResource]

  given Reads[Sprites] =
    (__ \ "frontDefault").readNullable[String].map(Sprites.apply)

  given Reads[PokemonType] = (json: JsValue) =>
    for
      slot     <- (json \ "slot").validate[Int]
      typeName <- (json \ "type" \ "name").validate[String]
    yield PokemonType(slot, typeName)

  given Reads[PokemonAbility] = (json: JsValue) =>
    for
      name     <- (json \ "ability" \ "name").validate[String]
      isHidden <- (json \ "isHidden").validate[Boolean]
      slot     <- (json \ "slot").validate[Int]
    yield PokemonAbility(name, isHidden, slot)

  given Reads[PokemonStat] = (json: JsValue) =>
    for
      name     <- (json \ "stat" \ "name").validate[String]
      baseStat <- (json \ "baseStat").validate[Int]
      effort   <- (json \ "effort").validate[Int]
    yield PokemonStat(name, baseStat, effort)

  // Parsea el objeto pokemon crudo (sin envelope)
  private def parsePokemon(json: JsValue): JsResult[Pokemon] =
    for
      id        <- (json \ "id").validate[Int]
      name      <- (json \ "name").validate[String]
      baseExp   <- (json \ "baseExperience").validateOpt[Int]
      height    <- (json \ "height").validate[Int]
      weight    <- (json \ "weight").validate[Int]
      abilities <- (json \ "abilities").validate[List[PokemonAbility]]
      stats     <- (json \ "stats").validate[List[PokemonStat]]
      types     <- (json \ "types").validate[List[PokemonType]]
      sprites   <- (json \ "sprites").validate[Sprites]
    yield Pokemon(id, name, baseExp, height, weight, abilities, stats, types, sprites)

  // Todas las respuestas del backend vienen envueltas en { "data": ..., "meta": ... }
  given Reads[Pokemon] = (json: JsValue) =>
    (json \ "data").validate[JsValue].flatMap(parsePokemon)

  given Reads[PaginatedResponse] = (json: JsValue) =>
    for
      count   <- (json \ "data" \ "count").validate[Int]
      results <- (json \ "data" \ "results").validate[List[NamedResource]]
    yield PaginatedResponse(count, results)
