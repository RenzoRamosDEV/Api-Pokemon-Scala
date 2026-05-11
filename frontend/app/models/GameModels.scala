package models

import play.api.libs.json.*

// ── Move ─────────────────────────────────────────────────────────
case class Move(
    id: Int,
    name: String,
    accuracy: Option[Int],
    pp: Int,
    priority: Int,
    power: Option[Int],
    damageClass: String,
    moveType: String,
    shortEffect: String
)

// ── Nature ────────────────────────────────────────────────────────
case class Nature(
    id: Int,
    name: String,
    decreasedStat: Option[String],
    increasedStat: Option[String],
    hatesFlavor: Option[String],
    likesFlavor: Option[String]
)

// ── Item ──────────────────────────────────────────────────────────
case class Item(
    id: Int,
    name: String,
    cost: Int,
    category: String,
    sprite: Option[String],
    shortEffect: String
)

// ── Berry ─────────────────────────────────────────────────────────
case class Berry(
    id: Int,
    name: String,
    growthTime: Int,
    maxHarvest: Int,
    naturalGiftPower: Int,
    naturalGiftType: String,
    smoothness: Int,
    firmness: String,
    flavors: List[BerryFlavorMap]
)

case class BerryFlavorMap(potency: Int, flavor: String)

// ── Type ──────────────────────────────────────────────────────────
case class GameType(
    id: Int,
    name: String,
    doubleDamageTo: List[String],
    halfDamageTo: List[String],
    noDamageTo: List[String],
    doubleDamageFrom: List[String],
    halfDamageFrom: List[String],
    noDamageFrom: List[String],
    pokemonCount: Int
)

// ── Ability ───────────────────────────────────────────────────────
case class Ability(
    id: Int,
    name: String,
    shortEffect: String,
    pokemonCount: Int
)

object GameModels:

  private def englishShortEffect(entries: JsValue): String =
    entries.asOpt[JsArray]
      .flatMap(_.value.find(e => (e \ "language" \ "name").asOpt[String].contains("en")))
      .flatMap(e => (e \ "shortEffect").asOpt[String])
      .getOrElse("—")

  private def nameOf(json: JsValue, field: String): Option[String] =
    (json \ field \ "name").asOpt[String]

  private def namesOf(json: JsValue, field: String): List[String] =
    (json \ field).asOpt[JsArray]
      .map(_.value.flatMap(v => (v \ "name").asOpt[String]).toList)
      .getOrElse(Nil)

  given Reads[Move] = (json: JsValue) =>
    val d = (json \ "data").getOrElse(json)
    for
      id          <- (d \ "id").validate[Int]
      name        <- (d \ "name").validate[String]
      accuracy    <- (d \ "accuracy").validateOpt[Int]
      pp          <- (d \ "pp").validate[Int]
      priority    <- (d \ "priority").validate[Int]
      power       <- (d \ "power").validateOpt[Int]
      damageClass <- (d \ "damageClass" \ "name").validate[String]
      moveType    <- (d \ "moveType" \ "name").validate[String]
    yield Move(id, name, accuracy, pp, priority, power, damageClass, moveType,
               englishShortEffect(d \ "effectEntries"))

  given Reads[Nature] = (json: JsValue) =>
    val d = (json \ "data").getOrElse(json)
    for
      id   <- (d \ "id").validate[Int]
      name <- (d \ "name").validate[String]
    yield Nature(
      id, name,
      nameOf(d, "decreasedStat"),
      nameOf(d, "increasedStat"),
      nameOf(d, "hatesFlavor"),
      nameOf(d, "likesFlavor")
    )

  given Reads[Item] = (json: JsValue) =>
    val d = (json \ "data").getOrElse(json)
    for
      id       <- (d \ "id").validate[Int]
      name     <- (d \ "name").validate[String]
      cost     <- (d \ "cost").validate[Int]
      category <- (d \ "category" \ "name").validate[String]
    yield Item(
      id, name, cost, category,
      (d \ "sprites" \ "default").asOpt[String],
      englishShortEffect(d \ "effectEntries")
    )

  given Reads[BerryFlavorMap] = (json: JsValue) =>
    for
      potency <- (json \ "potency").validate[Int]
      flavor  <- (json \ "flavor" \ "name").validate[String]
    yield BerryFlavorMap(potency, flavor)

  given Reads[Berry] = (json: JsValue) =>
    val d = (json \ "data").getOrElse(json)
    for
      id              <- (d \ "id").validate[Int]
      name            <- (d \ "name").validate[String]
      growthTime      <- (d \ "growthTime").validate[Int]
      maxHarvest      <- (d \ "maxHarvest").validate[Int]
      naturalGiftPower <- (d \ "naturalGiftPower").validate[Int]
      naturalGiftType <- (d \ "naturalGiftType" \ "name").validate[String]
      smoothness      <- (d \ "smoothness").validate[Int]
      firmness        <- (d \ "firmness" \ "name").validate[String]
      flavors         <- (d \ "flavors").validate[List[BerryFlavorMap]]
    yield Berry(id, name, growthTime, maxHarvest, naturalGiftPower, naturalGiftType,
                smoothness, firmness, flavors)

  given Reads[GameType] = (json: JsValue) =>
    val d  = (json \ "data").getOrElse(json)
    val dr = d \ "damageRelations"
    for
      id   <- (d \ "id").validate[Int]
      name <- (d \ "name").validate[String]
      pkCount = (d \ "pokemon").asOpt[JsArray].map(_.value.length).getOrElse(0)
    yield GameType(
      id, name,
      namesOf(dr, "doubleDamageTo"),
      namesOf(dr, "halfDamageTo"),
      namesOf(dr, "noDamageTo"),
      namesOf(dr, "doubleDamageFrom"),
      namesOf(dr, "halfDamageFrom"),
      namesOf(dr, "noDamageFrom"),
      pkCount
    )

  given Reads[Ability] = (json: JsValue) =>
    val d = (json \ "data").getOrElse(json)
    for
      id   <- (d \ "id").validate[Int]
      name <- (d \ "name").validate[String]
      pkCount = (d \ "pokemon").asOpt[JsArray].map(_.value.length).getOrElse(0)
    yield Ability(id, name, englishShortEffect(d \ "effectEntries"), pkCount)
