package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

// Tipo elemental (Fuego, Agua, Planta, etc.), mapeado desde GET /type/{id} de PokeAPI.
// Contiene la tabla de efectividades completa y los Pokémon/movimientos de ese tipo.
case class Type(
    id: Int,
    name: String,
    damageRelations: TypeDamageRelations,
    pokemon: List[TypePokemon],    // Pokémon que tienen este tipo (puede ser muy larga)
    moves: List[NamedResource]     // Movimientos de este tipo
)

// Tabla de efectividades del tipo: qué tipos recibe y a cuáles afecta con distintos multiplicadores
case class TypeDamageRelations(
    noDamageTo: List[NamedResource],       // Tipos a los que este tipo hace 0x daño
    halfDamageTo: List[NamedResource],     // Tipos a los que este tipo hace 0.5x daño
    doubleDamageTo: List[NamedResource],   // Tipos a los que este tipo hace 2x daño
    noDamageFrom: List[NamedResource],     // Tipos que hacen 0x daño a este tipo
    halfDamageFrom: List[NamedResource],   // Tipos que hacen 0.5x daño a este tipo
    doubleDamageFrom: List[NamedResource]  // Tipos que hacen 2x daño a este tipo
)

// Referencia a un Pokémon de este tipo, con el slot que ocupa (1 = tipo primario, 2 = secundario)
case class TypePokemon(slot: Int, pokemon: NamedResource)
