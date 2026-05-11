package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

case class Berry(
    id: Int,
    name: String,
    growthTime: Int,
    maxHarvest: Int,
    naturalGiftPower: Int,
    size: Int,
    smoothness: Int,
    soilDryness: Int,
    firmness: NamedResource,
    flavors: List[BerryFlavorMap],
    item: NamedResource,
    naturalGiftType: NamedResource
)

case class BerryFlavorMap(potency: Int, flavor: NamedResource)
