package com.pokeapp.domain.model.shared

// Referencia ligera que PokeAPI usa para representar cualquier recurso relacionado.
// Aparece en listas, abilities, moves, types, etc.
// La `url` apunta al endpoint completo del recurso en PokeAPI.
case class NamedResource(name: String, url: String)
