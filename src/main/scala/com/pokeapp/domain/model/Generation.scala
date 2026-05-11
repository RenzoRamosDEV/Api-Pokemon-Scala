package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

case class Generation(
    id: Int,
    name: String,
    abilities: List[NamedResource],
    moves: List[NamedResource],
    pokemonSpecies: List[NamedResource],
    types: List[NamedResource],
    versionGroups: List[NamedResource],
    mainRegion: NamedResource
)
