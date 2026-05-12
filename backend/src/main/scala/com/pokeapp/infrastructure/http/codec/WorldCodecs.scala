package com.pokeapp.infrastructure.http.codec

import com.pokeapp.domain.model.Location
import com.pokeapp.domain.model.shared.NamedResource
import io.circe.{Decoder, HCursor}

object WorldCodecs:

  given Decoder[Location] = (c: HCursor) =>
    for
      id     <- c.downField("id").as[Int]
      name   <- c.downField("name").as[String]
      region <- c.downField("region").as[Option[NamedResource]]
      areas  <- c.downField("areas").as[List[NamedResource]]
    yield Location(id, name, region, areas)
