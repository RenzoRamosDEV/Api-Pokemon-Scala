package com.pokeapp.application.pokemon

import cats.effect.Concurrent
import cats.syntax.all.*
import com.pokeapp.domain.error.DomainError
import com.pokeapp.domain.model.{Pokemon, PokemonFull}
import com.pokeapp.domain.model.shared.{NamedResource, PaginatedResponse}
import com.pokeapp.domain.port.PokemonRepository
import com.pokeapp.infrastructure.http.client.PokeApiClient

// Obtiene un Pokémon por ID o nombre desde el repositorio (que puede ser cacheado o HTTP).
class GetPokemonUseCase[F[_]](repo: PokemonRepository[F]):
  def execute(id: Int): F[Either[DomainError, Pokemon]]            = repo.findById(id)
  def executeByName(name: String): F[Either[DomainError, Pokemon]] = repo.findByName(name)

// Retorna una página de Pokémon (solo nombre y URL, sin detalles).
class ListPokemonUseCase[F[_]](repo: PokemonRepository[F]):
  def execute(
      limit: Int = 20,
      offset: Int = 0
  ): F[Either[DomainError, PaginatedResponse[NamedResource]]] =
    repo.list(limit, offset)

// Caso de uso enriquecido que combina tres llamadas a PokeAPI en una sola respuesta.
// A diferencia de GetPokemonUseCase, depende directamente de PokeApiClient porque necesita
// acceder a endpoints distintos (pokemon, pokemon-species y evolution-chain) cuya orquestación
// no encaja en el puerto PokemonRepository (que solo modela un tipo de recurso).
class GetPokemonFullUseCase[F[_]: Concurrent](client: PokeApiClient[F]):

  // El flujo es secuencial: pokemon → species → evolution chain.
  // Si pokemon o species fallan, se propaga el error (son datos críticos).
  // Si la cadena evolutiva falla, se devuelve PokemonFull con evolutionChain = None
  // para no bloquear la respuesta por un dato secundario.
  def execute(idOrName: String): F[Either[DomainError, PokemonFull]] =
    client.getPokemon(idOrName).flatMap:
      case Left(err) => err.asLeft[PokemonFull].pure[F]
      case Right(pokemon) =>
        client.getPokemonSpecies(idOrName).flatMap:
          case Left(err) => err.asLeft[PokemonFull].pure[F]
          case Right(species) =>
            species.evolutionChain match
              case None =>
                // Pokémon sin cadena evolutiva registrada (casos raros en PokeAPI)
                PokemonFull(pokemon, species, None).asRight[DomainError].pure[F]
              case Some(ref) =>
                client.getEvolutionChainByUrl(ref.url).map:
                  case Left(_)      => PokemonFull(pokemon, species, None).asRight[DomainError]
                  case Right(chain) => PokemonFull(pokemon, species, Some(chain)).asRight[DomainError]
