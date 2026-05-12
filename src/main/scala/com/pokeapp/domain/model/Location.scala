package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

// Localización del mundo Pokémon, mapeada desde GET /location/{id} de PokeAPI.
// `region` es None para localizaciones que no pertenecen a ninguna región (casos raros).
// `areas` son las sub-zonas dentro de esta localización donde se pueden encontrar Pokémon.
case class Location(
    id: Int,
    name: String,
    region: Option[NamedResource],
    areas: List[NamedResource]
)
