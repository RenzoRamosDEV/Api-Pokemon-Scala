package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

case class Item(
    id: Int,
    name: String,
    cost: Int,
    flingPower: Option[Int],
    flingEffect: Option[NamedResource],
    attributes: List[NamedResource],
    category: NamedResource,
    effectEntries: List[ItemEffect],
    sprites: ItemSprites
)

case class ItemEffect(effect: String, shortEffect: String, language: NamedResource)

case class ItemSprites(default: Option[String])
