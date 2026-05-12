package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

// Baya (Berry) del mundo Pokémon, mapeada desde GET /berry/{id} de PokeAPI.
// Las bayas se usan en batalla, para cocinar y para cultivar en los juegos.
case class Berry(
    id: Int,
    name: String,
    growthTime: Int,         // Horas que tarda en crecer una fase
    maxHarvest: Int,         // Máximo de bayas que puede dar una planta
    naturalGiftPower: Int,   // Potencia del movimiento Dádiva Natural al usar esta baya
    size: Int,               // Tamaño en milímetros
    smoothness: Int,         // Suavidad; afecta la sensación al hacer Pokéblocks/Poffins
    soilDryness: Int,        // Velocidad con la que seca el suelo al cultivarla
    firmness: NamedResource,
    flavors: List[BerryFlavorMap],   // Potencia de cada sabor que aporta la baya
    item: NamedResource,             // Item correspondiente a esta baya en el inventario
    naturalGiftType: NamedResource   // Tipo del movimiento Dádiva Natural con esta baya
)

// Potencia que aporta esta baya a un sabor específico (picante, dulce, etc.)
case class BerryFlavorMap(potency: Int, flavor: NamedResource)
