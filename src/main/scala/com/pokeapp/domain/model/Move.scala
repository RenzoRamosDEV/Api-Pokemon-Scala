package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

// Movimiento de combate, mapeado desde GET /move/{id} de PokeAPI.
// `accuracy` y `power` son None en movimientos que nunca fallan o no hacen daño directo.
case class Move(
    id: Int,
    name: String,
    accuracy: Option[Int],      // Precisión de 0 a 100; None si el movimiento no puede fallar
    pp: Int,                    // Puntos de poder (usos máximos del movimiento)
    priority: Int,              // Prioridad en el turno; positivo va antes, negativo va después
    power: Option[Int],         // Potencia base; None en movimientos de estado
    damageClass: NamedResource, // physical, special o status
    moveType: NamedResource,    // Tipo del movimiento (Fuego, Agua, etc.)
    target: NamedResource,      // A quién afecta (un enemigo, todos, el usuario, etc.)
    effectEntries: List[MoveEffect],
    meta: Option[MoveMeta]      // Datos avanzados de combate; None en movimientos simples
)

// Descripción del efecto del movimiento en un idioma concreto
case class MoveEffect(effect: String, shortEffect: String, language: NamedResource)

// Metadatos de combate avanzados del movimiento
case class MoveMeta(
    ailment: NamedResource,      // Alteración de estado que puede causar (parálisis, quemadura, etc.)
    category: NamedResource,     // Categoría táctica del movimiento (daño, curación, etc.)
    minHits: Option[Int],        // Golpes mínimos en movimientos multi-golpe
    maxHits: Option[Int],        // Golpes máximos en movimientos multi-golpe
    minTurns: Option[Int],       // Turnos mínimos de efecto (movimientos de varios turnos)
    maxTurns: Option[Int],       // Turnos máximos de efecto
    drain: Int,                  // Porcentaje de HP drenado al objetivo (negativo = recoil)
    healing: Int,                // Porcentaje de HP máximo que recupera el usuario
    critRate: Int,               // Incremento en la tasa de golpe crítico
    ailmentChance: Int,          // Probabilidad (0-100) de causar la alteración de estado
    flinchChance: Int,           // Probabilidad de hacer retroceder al objetivo
    statChance: Int              // Probabilidad de cambiar una estadística
)
