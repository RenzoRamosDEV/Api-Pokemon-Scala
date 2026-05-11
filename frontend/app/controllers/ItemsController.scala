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
class ItemsController @Inject() (
    cc: ControllerComponents,
    ws: WSClient,
    config: Configuration
)(implicit ec: ExecutionContext)
    extends AbstractController(cc):

  private val baseUrl  = config.get[String]("pokeapi.base-url")
  private val pageSize = 12

  def index(page: Int) = Action.async { implicit request =>
    val offset = (page - 1) * pageSize
    for
      listResp <- ws.url(s"$baseUrl/api/v1/item?limit=$pageSize&offset=$offset").get()
      paginated = listResp.json.as[PaginatedResponse]
      items    <- Future.sequence(paginated.results.map(r => fetchItem(r.name)))
      valid     = items.flatten.sortBy(_.id)
    yield
      val totalPages = math.ceil(paginated.count.toDouble / pageSize).toInt
      Ok(views.html.items(valid, page, totalPages))
  }

  private def fetchItem(name: String): Future[Option[Item]] =
    ws.url(s"$baseUrl/api/v1/item/$name")
      .get()
      .map(r => if r.status == 200 then r.json.asOpt[Item] else None)
      .recover(_ => None)
