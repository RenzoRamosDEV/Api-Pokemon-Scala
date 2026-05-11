package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

case class Type(
    id: Int,
    name: String,
    damageRelations: TypeDamageRelations,
    pokemon: List[TypePokemon],
    moves: List[NamedResource]
)

case class TypeDamageRelations(
    noDamageTo: List[NamedResource],
    halfDamageTo: List[NamedResource],
    doubleDamageTo: List[NamedResource],
    noDamageFrom: List[NamedResource],
    halfDamageFrom: List[NamedResource],
    doubleDamageFrom: List[NamedResource]
)

case class TypePokemon(slot: Int, pokemon: NamedResource)
