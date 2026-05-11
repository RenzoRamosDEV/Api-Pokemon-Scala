package com.pokeapp.infrastructure.http.codec

import com.pokeapp.domain.model.*
import com.pokeapp.domain.model.shared.NamedResource
import com.pokeapp.infrastructure.http.codec.PokemonCodec.given
import io.circe.{Decoder, HCursor}

// Decoders para PokemonSpecies, separados de PokemonCodec porque
// se usan solo en GetPokemonFullUseCase (no en cada request de pokemon).
object PokemonSpeciesCodec:

  // La cadena evolutiva viene como objeto { "url": "..." } dentro de la especie
  given Decoder[EvolutionChainRef] = (c: HCursor) =>
    c.downField("url").as[String].map(EvolutionChainRef.apply)

  given Decoder[FlavorTextEntry] = (c: HCursor) =>
    for
      flavorText <- c.downField("flavor_text").as[String]
      language   <- c.downField("language").as[NamedResource]
      version    <- c.downField("version").as[NamedResource]
    yield FlavorTextEntry(flavorText, language, version)

  given Decoder[PokemonSpecies] = (c: HCursor) =>
    for
      id                <- c.downField("id").as[Int]
      name              <- c.downField("name").as[String]
      isBaby            <- c.downField("is_baby").as[Boolean]
      isLegendary       <- c.downField("is_legendary").as[Boolean]
      isMythical        <- c.downField("is_mythical").as[Boolean]
      captureRate       <- c.downField("capture_rate").as[Int]
      baseHappiness     <- c.downField("base_happiness").as[Option[Int]]
      genderRate        <- c.downField("gender_rate").as[Int]
      flavorTextEntries <- c.downField("flavor_text_entries").as[List[FlavorTextEntry]]
      evolutionChain    <- c.downField("evolution_chain").as[Option[EvolutionChainRef]]
    yield PokemonSpecies(id, name, isBaby, isLegendary, isMythical, captureRate, baseHappiness, genderRate, flavorTextEntries, evolutionChain)
