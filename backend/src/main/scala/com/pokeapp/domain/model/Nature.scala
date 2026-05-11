package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

case class Nature(
    id: Int,
    name: String,
    decreasedStat: Option[NamedResource],
    increasedStat: Option[NamedResource],
    hatesFlavor: Option[NamedResource],
    likesFlavor: Option[NamedResource],
    statChanges: List[NatureStatChange],
    pokeathlonStatChanges: List[NaturePokeathlonStatChange]
)

case class NatureStatChange(maxChange: Int, stat: NamedResource)

case class NaturePokeathlonStatChange(maxChange: Int, stat: NamedResource)
