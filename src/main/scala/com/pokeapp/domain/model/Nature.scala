package com.pokeapp.domain.model

import com.pokeapp.domain.model.shared.NamedResource

// Naturaleza de un Pokémon, mapeada desde GET /nature/{id} de PokeAPI.
// Las naturalezas afectan el crecimiento de dos estadísticas: una sube un 10% y otra baja un 10%.
// Las naturalezas neutras (Seria, Tímida, etc.) tienen decreasedStat e increasedStat como None.
case class Nature(
    id: Int,
    name: String,
    decreasedStat: Option[NamedResource],  // Estadística que crece un 10% menos; None en naturalezas neutras
    increasedStat: Option[NamedResource],  // Estadística que crece un 10% más; None en naturalezas neutras
    hatesFlavor: Option[NamedResource],    // Sabor de baya que este Pokémon no come voluntariamente
    likesFlavor: Option[NamedResource],    // Sabor de baya que este Pokémon prefiere
    statChanges: List[NatureStatChange],
    pokeathlonStatChanges: List[NaturePokeathlonStatChange]
)

// Cambio en una estadística principal causado por la naturaleza
case class NatureStatChange(maxChange: Int, stat: NamedResource)

// Cambio en una estadística del Pokéatlón causado por la naturaleza
case class NaturePokeathlonStatChange(maxChange: Int, stat: NamedResource)
