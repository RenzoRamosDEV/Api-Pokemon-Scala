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
class TypesController @Inject() (
    cc: ControllerComponents,
    ws: WSClient,
    config: Configuration
)(implicit ec: ExecutionContext)
    extends AbstractController(cc):

  private val baseUrl = config.get[String]("pokeapi.base-url")

  def index = Action.async { implicit request =>
    for
      listResp <- ws.url(s"$baseUrl/api/v1/type?limit=20&offset=0").get()
      paginated = listResp.json.as[PaginatedResponse]
      types    <- Future.sequence(paginated.results.map(r => fetchType(r.name)))
      // IDs > 18 son tipos legacy (shadow, unknown) con datos incompletos
      valid     = types.flatten.filter(_.id <= 18).sortBy(_.id)
    yield Ok(views.html.types(valid))
  }

  private def fetchType(name: String): Future[Option[GameType]] =
    ws.url(s"$baseUrl/api/v1/type/$name")
      .get()
      .map(r => if r.status == 200 then r.json.asOpt[GameType] else None)
      .recover(_ => None)
