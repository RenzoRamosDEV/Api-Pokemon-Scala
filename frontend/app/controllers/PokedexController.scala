package controllers

import javax.inject.*
import play.api.mvc.*
import play.api.libs.ws.*
import play.api.Configuration
import models.*
import models.PokemonModels.given
import scala.concurrent.*

@Singleton
class PokedexController @Inject() (
    cc: ControllerComponents,
    ws: WSClient,
    config: Configuration
)(implicit ec: ExecutionContext)
    extends AbstractController(cc):

  private val baseUrl   = config.get[String]("pokeapi.base-url")
  private val pageSize  = 20

  def index(page: Int) = Action.async { implicit request =>
    val offset = (page - 1) * pageSize
    for
      listResp  <- ws.url(s"$baseUrl/api/v1/pokemon?limit=$pageSize&offset=$offset").get()
      paginated  = listResp.json.as[PaginatedResponse]
      pokemons  <- Future.sequence(paginated.results.map(r => fetchByName(r.name)))
      valid      = pokemons.flatten.sortBy(_.id)
    yield
      val totalPages = math.ceil(paginated.count.toDouble / pageSize).toInt
      Ok(views.html.index(valid, page, totalPages))
  }

  private def fetchByName(name: String): Future[Option[Pokemon]] =
    ws.url(s"$baseUrl/api/v1/pokemon/$name")
      .get()
      .map(resp => if resp.status == 200 then resp.json.asOpt[Pokemon] else None)
      .recover(_ => None)
