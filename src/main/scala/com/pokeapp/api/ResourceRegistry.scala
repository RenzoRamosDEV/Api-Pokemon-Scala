package com.pokeapp.api

// Lista blanca de todos los recursos disponibles en PokeAPI.
// Usada por GenericResourceRoutes y SwaggerRoutes para:
//   1. Validar que el path solicitado sea un recurso real de PokeAPI (no proxy abierto)
//   2. Generar automáticamente los paths del OpenAPI spec en SwaggerRoutes
//
// El Map es: nombre del path en nuestra API → path en PokeAPI (actualmente son iguales).
// Tenerlos separados permite redirigir o renombrar recursos sin cambiar la API pública.
object ResourceRegistry:
  val all: Map[String, String] = Map(
    "ability"                   -> "ability",
    "berry"                     -> "berry",
    "berry-firmness"            -> "berry-firmness",
    "berry-flavor"              -> "berry-flavor",
    "characteristic"            -> "characteristic",
    "contest-effect"            -> "contest-effect",
    "contest-type"              -> "contest-type",
    "egg-group"                 -> "egg-group",
    "encounter-condition"       -> "encounter-condition",
    "encounter-condition-value" -> "encounter-condition-value",
    "encounter-method"          -> "encounter-method",
    "evolution-chain"           -> "evolution-chain",
    "evolution-trigger"         -> "evolution-trigger",
    "gender"                    -> "gender",
    "generation"                -> "generation",
    "growth-rate"               -> "growth-rate",
    "item"                      -> "item",
    "item-attribute"            -> "item-attribute",
    "item-category"             -> "item-category",
    "item-fling-effect"         -> "item-fling-effect",
    "item-pocket"               -> "item-pocket",
    "language"                  -> "language",
    "location"                  -> "location",
    "location-area"             -> "location-area",
    "machine"                   -> "machine",
    "move"                      -> "move",
    "move-ailment"              -> "move-ailment",
    "move-battle-style"         -> "move-battle-style",
    "move-category"             -> "move-category",
    "move-damage-class"         -> "move-damage-class",
    "move-learn-method"         -> "move-learn-method",
    "move-target"               -> "move-target",
    "nature"                    -> "nature",
    "pal-park-area"             -> "pal-park-area",
    "pokeathlon-stat"           -> "pokeathlon-stat",
    "pokedex"                   -> "pokedex",
    "pokemon"                   -> "pokemon",
    "pokemon-color"             -> "pokemon-color",
    "pokemon-form"              -> "pokemon-form",
    "pokemon-habitat"           -> "pokemon-habitat",
    "pokemon-shape"             -> "pokemon-shape",
    "pokemon-species"           -> "pokemon-species",
    "region"                    -> "region",
    "stat"                      -> "stat",
    "super-contest-effect"      -> "super-contest-effect",
    "type"                      -> "type",
    "version"                   -> "version",
    "version-group"             -> "version-group"
  )
