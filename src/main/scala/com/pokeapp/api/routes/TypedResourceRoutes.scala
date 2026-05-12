package com.pokeapp.api.routes

import cats.effect.Concurrent
import cats.syntax.all.*
import com.pokeapp.api.dto.DomainEncoders.given
import com.pokeapp.api.dto.ResponseCodec.given
import com.pokeapp.application.ability.{GetAbilityUseCase, ListAbilityUseCase}
import com.pokeapp.application.berry.{GetBerryUseCase, ListBerryUseCase}
import com.pokeapp.application.evolution.{GetEvolutionChainUseCase, ListEvolutionChainUseCase}
import com.pokeapp.application.item.{GetItemUseCase, ListItemUseCase}
import com.pokeapp.application.location.{GetLocationUseCase, ListLocationUseCase}
import com.pokeapp.application.move.{GetMoveUseCase, ListMoveUseCase}
import com.pokeapp.application.nature.{GetNatureUseCase, ListNatureUseCase}
import com.pokeapp.application.`type`.{GetTypeUseCase, ListTypeUseCase}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

// Rutas HTTP para los recursos tipados (Ability, Berry, Move, Type, Item, Nature, Evolution, Location).
// Cada recurso expone GET /{resource} (lista paginada) y GET /{resource}/{id|name} (detalle).
// Extiende RouteSupport para reutilizar handleTyped, handleList y validatePagination.
class TypedResourceRoutes[F[_]: Concurrent](
    getAbility: GetAbilityUseCase[F],
    listAbility: ListAbilityUseCase[F],
    getBerry: GetBerryUseCase[F],
    listBerry: ListBerryUseCase[F],
    getMove: GetMoveUseCase[F],
    listMove: ListMoveUseCase[F],
    getType: GetTypeUseCase[F],
    listType: ListTypeUseCase[F],
    getItem: GetItemUseCase[F],
    listItem: ListItemUseCase[F],
    getNature: GetNatureUseCase[F],
    listNature: ListNatureUseCase[F],
    getEvolution: GetEvolutionChainUseCase[F],
    listEvolution: ListEvolutionChainUseCase[F],
    getLocation: GetLocationUseCase[F],
    listLocation: ListLocationUseCase[F]
) extends RouteSupport[F]:
  import dsl.*

  private object LimitParam  extends OptionalQueryParamDecoderMatcher[Int]("limit")
  private object OffsetParam extends OptionalQueryParamDecoderMatcher[Int]("offset")

  // Despacha por ID numérico o nombre de texto: si `id` parsea a Int llama a `byId`, si no a `byName`
  private def idOrName[A](
      id: String,
      byId: Int => F[Either[com.pokeapp.domain.error.DomainError, A]],
      byName: String => F[Either[com.pokeapp.domain.error.DomainError, A]]
  )(using io.circe.Encoder[A]): F[org.http4s.Response[F]] =
    id.toIntOption.fold(byName(id))(byId).flatMap(handleTyped)

  // Valida los parámetros de paginación y, si son válidos, ejecuta el listado
  private def listWithValidation[A: io.circe.Encoder](
      limit: Option[Int],
      offset: Option[Int],
      execute: (Int, Int) => F[Either[com.pokeapp.domain.error.DomainError, A]]
  ): F[org.http4s.Response[F]] =
    val l = limit.getOrElse(20)
    val o = offset.getOrElse(0)
    validatePagination(l, o).getOrElse(execute(l, o).flatMap(handleList))

  val routes: HttpRoutes[F] = HttpRoutes.of[F]:
    // Ability
    case GET -> Root / "api" / "v1" / "ability" :? LimitParam(l) +& OffsetParam(o) =>
      listWithValidation(l, o, listAbility.execute)
    case GET -> Root / "api" / "v1" / "ability" / id =>
      idOrName(id, getAbility.execute, getAbility.executeByName)

    // Berry
    case GET -> Root / "api" / "v1" / "berry" :? LimitParam(l) +& OffsetParam(o) =>
      listWithValidation(l, o, listBerry.execute)
    case GET -> Root / "api" / "v1" / "berry" / id =>
      idOrName(id, getBerry.execute, getBerry.executeByName)

    // Move
    case GET -> Root / "api" / "v1" / "move" :? LimitParam(l) +& OffsetParam(o) =>
      listWithValidation(l, o, listMove.execute)
    case GET -> Root / "api" / "v1" / "move" / id =>
      idOrName(id, getMove.execute, getMove.executeByName)

    // Type
    case GET -> Root / "api" / "v1" / "type" :? LimitParam(l) +& OffsetParam(o) =>
      listWithValidation(l, o, listType.execute)
    case GET -> Root / "api" / "v1" / "type" / id =>
      idOrName(id, getType.execute, getType.executeByName)

    // Item
    case GET -> Root / "api" / "v1" / "item" :? LimitParam(l) +& OffsetParam(o) =>
      listWithValidation(l, o, listItem.execute)
    case GET -> Root / "api" / "v1" / "item" / id =>
      idOrName(id, getItem.execute, getItem.executeByName)

    // Nature
    case GET -> Root / "api" / "v1" / "nature" :? LimitParam(l) +& OffsetParam(o) =>
      listWithValidation(l, o, listNature.execute)
    case GET -> Root / "api" / "v1" / "nature" / id =>
      idOrName(id, getNature.execute, getNature.executeByName)

    // Evolution chain
    case GET -> Root / "api" / "v1" / "evolution-chain" :? LimitParam(l) +& OffsetParam(o) =>
      listWithValidation(l, o, listEvolution.execute)
    case GET -> Root / "api" / "v1" / "evolution-chain" / IntVar(id) =>
      getEvolution.execute(id).flatMap(handleTyped)

    // Location
    case GET -> Root / "api" / "v1" / "location" :? LimitParam(l) +& OffsetParam(o) =>
      listWithValidation(l, o, listLocation.execute)
    case GET -> Root / "api" / "v1" / "location" / id =>
      idOrName(id, getLocation.execute, getLocation.executeByName)
