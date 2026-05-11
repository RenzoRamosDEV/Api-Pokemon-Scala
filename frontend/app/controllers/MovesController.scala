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
class MovesController @Inject() (
    cc: ControllerComponents,
    ws: WSClient,
    config: Configuration
)(implicit ec: ExecutionContext)
    extends AbstractController(cc):

  private val baseUrl  = config.get[String]("pokeapi.base-url")
  private val pageSize = 20

  def index(page: Int) = Action.async { implicit request =>
    val offset = (page - 1) * pageSize
    for
      listResp <- ws.url(s"$baseUrl/api/v1/move?limit=$pageSize&offset=$offset").get()
      paginated = listResp.json.as[PaginatedResponse]
      moves    <- Future.sequence(paginated.results.map(r => fetchMove(r.name)))
      valid     = moves.flatten.sortBy(_.id)
    yield
      val totalPages = math.ceil(paginated.count.toDouble / pageSize).toInt
      Ok(views.html.moves(valid, page, totalPages))
  }

  private def fetchMove(name: String): Future[Option[Move]] =
    ws.url(s"$baseUrl/api/v1/move/$name")
      .get()
      .map(r => if r.status == 200 then r.json.asOpt[Move] else None)
      .recover(_ => None)
