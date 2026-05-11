package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

case class Stat(
    id: Int,
    name: String,
    gameIndex: Int,
    isBattleOnly: Boolean,
    affectingMoves: StatAffectingMoves,
    affectingNatures: StatAffectingNatures
)

case class StatAffectingMoves(
    increase: List[MoveStatAffect],
    decrease: List[MoveStatAffect]
)

case class MoveStatAffect(change: Int, move: NamedResource)

case class StatAffectingNatures(
    increase: List[NamedResource],
    decrease: List[NamedResource]
)
