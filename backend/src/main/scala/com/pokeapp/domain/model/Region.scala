package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

case class Region(
    id: Int,
    name: String,
    locations: List[NamedResource],
    mainGeneration: Option[NamedResource],
    pokedexes: List[NamedResource],
    versionGroups: List[NamedResource]
)
