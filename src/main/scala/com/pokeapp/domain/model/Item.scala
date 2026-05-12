package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

// Item del mundo Pokémon, mapeado desde GET /item/{id} de PokeAPI.
// Incluye tanto items de batalla como de exploración y evolución.
case class Item(
    id: Int,
    name: String,
    cost: Int,                          // Precio en la tienda en PokéDólares; 0 si no se vende
    flingPower: Option[Int],            // Potencia del movimiento Lanzamiento; None si no se puede lanzar
    flingEffect: Option[NamedResource], // Efecto especial al lanzarlo; None si no aplica
    attributes: List[NamedResource],    // Propiedades del item (consumible, sostenible, etc.)
    category: NamedResource,
    effectEntries: List[ItemEffect],    // Descripciones del efecto por idioma
    sprites: ItemSprites
)

// Descripción del efecto de un item en un idioma concreto
case class ItemEffect(effect: String, shortEffect: String, language: NamedResource)

// Sprite del item (imagen de icono); None si PokeAPI no tiene imagen para este item
case class ItemSprites(default: Option[String])
