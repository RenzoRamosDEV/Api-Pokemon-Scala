package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

case class Move(
    id: Int,
    name: String,
    accuracy: Option[Int],
    pp: Int,
    priority: Int,
    power: Option[Int],
    damageClass: NamedResource,
    moveType: NamedResource,
    target: NamedResource,
    effectEntries: List[MoveEffect],
    meta: Option[MoveMeta]
)

case class MoveEffect(effect: String, shortEffect: String, language: NamedResource)

case class MoveMeta(
    ailment: NamedResource,
    category: NamedResource,
    minHits: Option[Int],
    maxHits: Option[Int],
    minTurns: Option[Int],
    maxTurns: Option[Int],
    drain: Int,
    healing: Int,
    critRate: Int,
    ailmentChance: Int,
    flinchChance: Int,
    statChance: Int
)
