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
class AbilitiesController @Inject() (
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
      listResp  <- ws.url(s"$baseUrl/api/v1/ability?limit=$pageSize&offset=$offset").get()
      paginated  = listResp.json.as[PaginatedResponse]
      abilities <- Future.sequence(paginated.results.map(r => fetchAbility(r.name)))
      valid      = abilities.flatten.sortBy(_.id)
    yield
      val totalPages = math.ceil(paginated.count.toDouble / pageSize).toInt
      Ok(views.html.abilities(valid, page, totalPages))
  }

  private def fetchAbility(name: String): Future[Option[Ability]] =
    ws.url(s"$baseUrl/api/v1/ability/$name")
      .get()
      .map(r => if r.status == 200 then r.json.asOpt[Ability] else None)
      .recover(_ => None)
