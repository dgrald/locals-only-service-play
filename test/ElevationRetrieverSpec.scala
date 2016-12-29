import org.scalatestplus.play.PlaySpec
import play.api.libs.json._
import play.api.mvc._
import play.api.routing.sird._
import play.api.test._
import play.core.server.Server
import services.{Constants, ElevationRetriever}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by dylangrald on 12/29/16.
  */
class ElevationRetrieverSpec extends PlaySpec {

  "ElevationRetriever.getElevation" should {
    val lat = SomeRandom.double()
    val long = SomeRandom.double()

    val elevationResult = SomeRandom.double()

    "call the google elevation API correctly" in {
      Server.withRouter() {
        case GET(p"/json") => Action { request =>
          val queries = request.queryString
          val locations = queries("locations").head
          val key = queries("key").head
          if(locations.equals(s"$lat,$long") && key.equals(Constants.googleApiKey)) {
            Results.Ok(Json.obj("results" ->
              Json.arr(Json.obj("elevation" -> elevationResult, "location" -> Json.obj("lat" -> lat, "lng" -> long))),
              "resolution" -> SomeRandom.double()))
          } else {
            Results.BadRequest("")
          }
        }
      } { implicit port =>
        WsTestClient.withClient { client =>
          val result = Await.result(new ElevationRetriever(client, "").retrieveElevation(lat, long), 10.seconds)

          result mustEqual elevationResult
        }
      }
    }
  }

}
