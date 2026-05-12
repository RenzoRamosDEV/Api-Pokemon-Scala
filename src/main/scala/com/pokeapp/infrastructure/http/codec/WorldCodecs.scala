package com.pokeapp.infrastructure.http.codec

import com.pokeapp.domain.model.Location
import com.pokeapp.domain.model.shared.NamedResource
import com.pokeapp.infrastructure.http.codec.PokemonCodec.given
import io.circe.{Decoder, HCursor}

// Decoders Circe para deserializar la respuesta JSON de /location/{id} al modelo de dominio.
// Importa el decoder de NamedResource desde PokemonCodec para reutilizarlo.
object WorldCodecs:

  // Deserializa el objeto location de PokeAPI; region puede ser null → None
  given Decoder[Location] = (c: HCursor) =>
    for
      id     <- c.downField("id").as[Int]
      name   <- c.downField("name").as[String]
      region <- c.downField("region").as[Option[NamedResource]]
      areas  <- c.downField("areas").as[List[NamedResource]]
    yield Location(id, name, region, areas)
