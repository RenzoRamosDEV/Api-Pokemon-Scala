package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

case class Pokedex(
    id: Int,
    name: String,
    isMainSeries: Boolean,
    region: Option[NamedResource],
    pokemonEntries: List[PokemonEntry]
)

case class PokemonEntry(entryNumber: Int, pokemonSpecies: NamedResource)
