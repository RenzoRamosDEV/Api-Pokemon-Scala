package com.pokeapp

import cats.effect.{IO, IOApp, Resource}
import com.pokeapp.api.Server
import com.pokeapp.api.routes.*
import com.pokeapp.application.ability.{GetAbilityUseCase, ListAbilityUseCase}
import com.pokeapp.application.berry.{GetBerryUseCase, ListBerryUseCase}
import com.pokeapp.application.evolution.{GetEvolutionChainUseCase, ListEvolutionChainUseCase}
import com.pokeapp.application.item.{GetItemUseCase, ListItemUseCase}
import com.pokeapp.application.location.{GetLocationUseCase, ListLocationUseCase}
import com.pokeapp.application.move.{GetMoveUseCase, ListMoveUseCase}
import com.pokeapp.application.nature.{GetNatureUseCase, ListNatureUseCase}
import com.pokeapp.application.pokemon.{GetPokemonFullUseCase, GetPokemonUseCase, ListPokemonUseCase}
import com.pokeapp.application.`type`.{GetTypeUseCase, ListTypeUseCase}
import com.pokeapp.config.AppConfig
import com.pokeapp.domain.model.Pokemon
import com.pokeapp.infrastructure.adapter.*
import com.pokeapp.infrastructure.cache.{CachedPokemonRepository, CaffeineCache}
import com.pokeapp.infrastructure.http.client.{GenericPokeApiClient, PokeApiClient}
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.{Logger, LoggerFactory}
import org.typelevel.log4cats.slf4j.Slf4jFactory
import pureconfig.ConfigSource

import scala.concurrent.duration.*

// Punto de entrada de la aplicación. Sigue el patrón de composición de Cats Effect:
// todo se construye como un Resource para garantizar cleanup correcto al apagarse.
object Main extends IOApp.Simple:

  given LoggerFactory[IO] = Slf4jFactory.create[IO]
  given Logger[IO]        = Slf4jFactory.create[IO].getLogger

  def run: IO[Unit] = program.useForever

  private def program: Resource[IO, Unit] =
    for
      // Carga configuración desde src/main/resources/application.conf
      config <- Resource.eval(IO(ConfigSource.default.loadOrThrow[AppConfig]))
      logger  = summon[LoggerFactory[IO]].getLogger

      // Cliente HTTP compartido por todos los adaptadores (Ember usa NIO internamente)
      httpClient <- EmberClientBuilder.default[IO].build

      // ── Clientes HTTP ────────────────────────────────────────────────────
      // PokeApiClient: deserializa a modelos de dominio tipados
      // GenericPokeApiClient: devuelve Json crudo para recursos sin modelo propio
      pokeApiClient = PokeApiClient[IO](httpClient, config.pokeapi)
      genericClient = GenericPokeApiClient[IO](httpClient, config.pokeapi)

      // ── Repositorios con caché ────────────────────────────────────────────
      // Solo Pokémon tiene caché dedicado porque es el recurso más consultado.
      // Los demás recursos van directo a PokeAPI en cada request.
      httpPokemonRepo = HttpPokemonRepository[IO](pokeApiClient)
      pokemonCache <- CaffeineCache.build[IO, String, Pokemon](
        config.cache.maxSize,
        config.cache.ttlMinutes.minutes
      )
      pokemonRepo = CachedPokemonRepository[IO](httpPokemonRepo, pokemonCache)

      // ── Repositorios HTTP tipados ─────────────────────────────────────────
      abilityRepo   = HttpAbilityRepository[IO](pokeApiClient)
      berryRepo     = HttpBerryRepository[IO](pokeApiClient)
      moveRepo      = HttpMoveRepository[IO](pokeApiClient)
      typeRepo      = HttpTypeRepository[IO](pokeApiClient)
      itemRepo      = HttpItemRepository[IO](pokeApiClient)
      natureRepo    = HttpNatureRepository[IO](pokeApiClient)
      evolutionRepo = HttpEvolutionRepository[IO](pokeApiClient)
      locationRepo  = HttpLocationRepository[IO](pokeApiClient)

      // ── Casos de uso ──────────────────────────────────────────────────────
      // GetPokemonFullUseCase recibe pokeApiClient directamente porque necesita
      // orquestar tres endpoints distintos (pokemon + species + evolution-chain)
      getPokemon     = GetPokemonUseCase(pokemonRepo)
      listPokemon    = ListPokemonUseCase(pokemonRepo)
      getPokemonFull = GetPokemonFullUseCase[IO](pokeApiClient)

      getAbility    = GetAbilityUseCase(abilityRepo)
      listAbility   = ListAbilityUseCase(abilityRepo)
      getBerry      = GetBerryUseCase(berryRepo)
      listBerry     = ListBerryUseCase(berryRepo)
      getMove       = GetMoveUseCase(moveRepo)
      listMove      = ListMoveUseCase(moveRepo)
      getType       = GetTypeUseCase(typeRepo)
      listType      = ListTypeUseCase(typeRepo)
      getItem       = GetItemUseCase(itemRepo)
      listItem      = ListItemUseCase(itemRepo)
      getNature     = GetNatureUseCase(natureRepo)
      listNature    = ListNatureUseCase(natureRepo)
      getEvolution  = GetEvolutionChainUseCase(evolutionRepo)
      listEvolution = ListEvolutionChainUseCase(evolutionRepo)
      getLocation   = GetLocationUseCase(locationRepo)
      listLocation  = ListLocationUseCase(locationRepo)

      // ── Rutas HTTP ────────────────────────────────────────────────────────
      pokemonRoutes = PokemonRoutes[IO](getPokemon, listPokemon, getPokemonFull)
      typedRoutes = TypedResourceRoutes[IO](
        getAbility, listAbility,
        getBerry, listBerry,
        getMove, listMove,
        getType, listType,
        getItem, listItem,
        getNature, listNature,
        getEvolution, listEvolution,
        getLocation, listLocation
      )
      genericRoutes = GenericResourceRoutes[IO](genericClient)
      healthRoutes  = HealthRoutes[IO]
      swaggerRoutes = SwaggerRoutes[IO]

      _ <- Server.build[IO](config.server, pokemonRoutes, typedRoutes, genericRoutes, healthRoutes, swaggerRoutes)
      _ <- Resource.eval(
        logger.info(s"PokéAPI server started on ${config.server.host}:${config.server.port}")
      )
    yield ()
