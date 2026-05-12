package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

// Modelo principal de un Pokémon, mapeado desde el endpoint GET /pokemon/{id} de PokeAPI.
// `height` y `weight` están en decímetros y hectogramos respectivamente (unidades de PokeAPI).
case class Pokemon(
    id: Int,
    name: String,
    baseExperience: Option[Int], // Algunos Pokémon no tienen valor de experiencia base definido
    height: Int,
    weight: Int,
    isDefault: Boolean,          // False en formas alternativas (mega, regional, etc.)
    abilities: List[PokemonAbility],
    moves: List[PokemonMove],
    stats: List[PokemonStat],
    types: List[PokemonType],
    sprites: Sprites
)

// Habilidad de un Pokémon con su slot y si es oculta
case class PokemonAbility(
    ability: NamedResource,
    isHidden: Boolean, // Las habilidades ocultas se obtienen por métodos especiales en el juego
    slot: Int
)

// Referencia a un movimiento que el Pokémon puede aprender
case class PokemonMove(
    move: NamedResource
)

// Estadística base de un Pokémon (HP, Ataque, Defensa, etc.)
case class PokemonStat(
    stat: NamedResource,
    baseStat: Int, // Valor base de la stat, sin EVs ni IVs
    effort: Int    // Puntos de esfuerzo (EVs) que otorga al derrotar este Pokémon
)

// Tipo del Pokémon (Fuego, Agua, etc.) con su slot (un Pokémon puede tener hasta 2 tipos)
case class PokemonType(
    slot: Int,
    `type`: NamedResource
)

// URLs de los sprites del Pokémon. Pueden ser null en PokeAPI si no existen para ese Pokémon.
case class Sprites(
    frontDefault: Option[String],
    frontShiny: Option[String],
    backDefault: Option[String],
    backShiny: Option[String]
)
