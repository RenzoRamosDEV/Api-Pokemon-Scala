package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

case class Location(
    id: Int,
    name: String,
    region: Option[NamedResource],
    areas: List[NamedResource]
)
