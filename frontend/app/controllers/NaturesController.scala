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
class NaturesController @Inject() (
    cc: ControllerComponents,
    ws: WSClient,
    config: Configuration
)(implicit ec: ExecutionContext)
    extends AbstractController(cc):

  private val baseUrl = config.get[String]("pokeapi.base-url")

  def index = Action.async { implicit request =>
    for
      listResp <- ws.url(s"$baseUrl/api/v1/nature?limit=25&offset=0").get()
      paginated = listResp.json.as[PaginatedResponse]
      natures  <- Future.sequence(paginated.results.map(r => fetchNature(r.name)))
      valid     = natures.flatten.sortBy(_.id)
    yield Ok(views.html.natures(valid))
  }

  private def fetchNature(name: String): Future[Option[Nature]] =
    ws.url(s"$baseUrl/api/v1/nature/$name")
      .get()
      .map(r => if r.status == 200 then r.json.asOpt[Nature] else None)
      .recover(_ => None)
