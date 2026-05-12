package com.pokeapp.api.dto

import com.pokeapp.domain.model.*
import com.pokeapp.domain.model.shared.NamedResource
import io.circe.{Encoder, Json}
import io.circe.generic.semiauto.*
import io.circe.syntax.*

// Encoders Circe para todos los modelos de dominio.
// `deriveEncoder` genera automáticamente el encoder desde los campos del case class.
// Los campos camelCase se serializan tal cual (no se convierten a snake_case).
// Importar con `import DomainEncoders.given` en las rutas que necesiten serializar modelos.
object DomainEncoders:

  // ── Pokémon ──────────────────────────────────────────────────────────────
  given Encoder[NamedResource]  = deriveEncoder
  given Encoder[Sprites]        = deriveEncoder
  given Encoder[PokemonAbility] = deriveEncoder
  given Encoder[PokemonMove]    = deriveEncoder
  given Encoder[PokemonStat]    = deriveEncoder
  given Encoder[PokemonType]    = deriveEncoder
  given Encoder[Pokemon]        = deriveEncoder

  // ── Ability ───────────────────────────────────────────────────────────────
  given Encoder[AbilityEffect]  = deriveEncoder
  given Encoder[AbilityPokemon] = deriveEncoder
  given Encoder[Ability]        = deriveEncoder

  // ── Berry ─────────────────────────────────────────────────────────────────
  given Encoder[BerryFlavorMap] = deriveEncoder
  given Encoder[Berry]          = deriveEncoder

  // ── Move ──────────────────────────────────────────────────────────────────
  given Encoder[MoveEffect] = deriveEncoder
  given Encoder[MoveMeta]   = deriveEncoder
  given Encoder[Move]       = deriveEncoder

  // ── Type ──────────────────────────────────────────────────────────────────
  given Encoder[TypeDamageRelations] = deriveEncoder
  given Encoder[TypePokemon]         = deriveEncoder
  given Encoder[Type]                = deriveEncoder

  // ── Item ──────────────────────────────────────────────────────────────────
  given Encoder[ItemEffect]  = deriveEncoder
  given Encoder[ItemSprites] = deriveEncoder
  given Encoder[Item]        = deriveEncoder

  // ── Nature ────────────────────────────────────────────────────────────────
  given Encoder[NatureStatChange]           = deriveEncoder
  given Encoder[NaturePokeathlonStatChange] = deriveEncoder
  given Encoder[Nature]                     = deriveEncoder

  // ── Evolution ─────────────────────────────────────────────────────────────
  given Encoder[EvolutionDetail] = deriveEncoder

  // ChainLink se codifica manualmente (no con deriveEncoder) porque es recursivo:
  // contiene List[ChainLink] en evolvesTo. Circe no puede derivar encoders recursivos
  // con semiauto, así que se usa una instancia explícita que se llama a sí misma.
  given chainLinkEncoder: Encoder[ChainLink] = Encoder.instance: cl =>
    Json.obj(
      "isBaby"           -> cl.isBaby.asJson,
      "species"          -> cl.species.asJson(using given_Encoder_NamedResource),
      "evolutionDetails" -> cl.evolutionDetails.asJson,
      "evolvesTo"        -> Json.arr(cl.evolvesTo.map(chainLinkEncoder.apply)*)
    )

  given Encoder[EvolutionChain] = deriveEncoder

  // ── World (Location) ──────────────────────────────────────────────────────
  given Encoder[Location] = deriveEncoder

  // ── PokemonFull (endpoint /pokemon/{id}/full) ─────────────────────────────
  given Encoder[EvolutionChainRef] = deriveEncoder
  given Encoder[FlavorTextEntry]   = deriveEncoder
  given Encoder[PokemonSpecies]    = deriveEncoder
  given Encoder[PokemonFull]       = deriveEncoder
