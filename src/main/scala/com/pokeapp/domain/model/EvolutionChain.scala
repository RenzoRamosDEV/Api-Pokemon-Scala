package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

// Cadena evolutiva completa de un grupo de Pokémon.
// La estructura es un árbol: `chain` es la raíz y puede ramificarse en `evolvesTo`.
case class EvolutionChain(
    id: Int,
    babyTriggerItem: Option[NamedResource], // Item necesario para obtener la pre-evolución bebé (ej: Incienso)
    chain: ChainLink
)

// Nodo del árbol evolutivo. Cada nodo representa una especie y sus condiciones de evolución.
// `evolvesTo` puede contener múltiples ramas (ej: Eevee → Vaporeon, Jolteon, Flareon, etc.)
case class ChainLink(
    isBaby: Boolean,
    species: NamedResource,
    evolutionDetails: List[EvolutionDetail], // Vacío para la forma base de la cadena
    evolvesTo: List[ChainLink]               // Vacío si es la forma final de la línea
)

// Condiciones específicas para que ocurra una evolución.
// La mayoría de los campos son None; solo los relevantes para cada evolución tienen valor.
case class EvolutionDetail(
    trigger: NamedResource,               // level-up, use-item, trade, etc.
    item: Option[NamedResource],          // Item para usar en la evolución (ej: Piedra Fuego)
    minLevel: Option[Int],                // Nivel mínimo para evolucionar por subida de nivel
    minHappiness: Option[Int],            // Felicidad mínima (ej: Eevee → Espeon)
    minBeauty: Option[Int],               // Belleza mínima en Concursos (solo gen III)
    minAffection: Option[Int],            // Cariño mínimo en Pokémon-Amie
    needsOverworldRain: Boolean,          // Evoluciona solo si llueve en el exterior
    heldItem: Option[NamedResource],      // Item que debe llevar al evolucionar (ej: Metal Coat)
    knownMove: Option[NamedResource],     // Movimiento que debe conocer (ej: Bonsly → Sudowoodo)
    knownMoveType: Option[NamedResource], // Tipo de movimiento que debe conocer
    location: Option[NamedResource],      // Lugar específico donde evolucionar (ej: Mossy Rock)
    partySpecies: Option[NamedResource],  // Pokémon que debe estar en el equipo
    partyType: Option[NamedResource],     // Tipo de Pokémon que debe estar en el equipo
    relativePhysicalStats: Option[Int],   // Comparativa Ataque vs Defensa (Tyrogue)
    timeOfDay: String,                    // "day", "night" o "" si no importa la hora
    tradeSpecies: Option[NamedResource],  // Pokémon específico con el que intercambiar
    turnUpsideDown: Boolean               // Consola boca abajo (Inkay → Malamar)
)
