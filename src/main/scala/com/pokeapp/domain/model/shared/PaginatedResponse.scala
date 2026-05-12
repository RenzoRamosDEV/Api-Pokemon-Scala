package com.pokeapp.domain.model.shared

// Respuesta paginada genérica que PokeAPI devuelve en todos sus endpoints de listado.
// `count`    → total de recursos disponibles en PokeAPI (no solo los de esta página)
// `next`     → URL de la siguiente página (None si estamos en la última)
// `previous` → URL de la página anterior (None si estamos en la primera)
// `results`  → los elementos de esta página
case class PaginatedResponse[A](
    count: Int,
    next: Option[String],
    previous: Option[String],
    results: List[A]
)
