package controllers

import javax.inject.*
import play.api.mvc.*
import play.api.libs.ws.*
import play.api.Configuration
import models.*
import models.PokemonModels.given
import models.GameModels.given
import scala.concurrent.*

@Singleton
class BerriesController @Inject() (
    cc: ControllerComponents,
    ws: WSClient,
    config: Configuration
)(implicit ec: ExecutionContext)
    extends AbstractController(cc):

  private val baseUrl  = config.get[String]("pokeapi.base-url")
  private val pageSize = 10

  def index(page: Int) = Action.async { implicit request =>
    val offset = (page - 1) * pageSize
    for
      listResp <- ws.url(s"$baseUrl/api/v1/berry?limit=$pageSize&offset=$offset").get()
      paginated = listResp.json.as[PaginatedResponse]
      berries  <- Future.sequence(paginated.results.map(r => fetchBerry(r.name)))
      valid     = berries.flatten.sortBy(_.id)
    yield
      val totalPages = math.ceil(paginated.count.toDouble / pageSize).toInt
      Ok(views.html.berries(valid, page, totalPages))
  }

  private def fetchBerry(name: String): Future[Option[Berry]] =
    ws.url(s"$baseUrl/api/v1/berry/$name")
      .get()
      .map(r => if r.status == 200 then r.json.asOpt[Berry] else None)
      .recover(_ => None)
