import org.scalatestplus.play.PlaySpec
import play.api.libs.json._
import play.api.mvc._
import play.api.routing.sird._
import play.api.test._
import play.core.server.Server
import services._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by dylangrald on 12/29/16.
  */
class ElevationRetrieverSpec extends PlaySpec {

  "ElevationRetriever.getElevation" should {

    "call the google elevation API correctly for a PointLocation" in {
      val pointLocation = SomeRandom.pointLocation()
      val elevationResult = SomeRandom.double()
      val lat = pointLocation.lat
      val long = pointLocation.long
      Server.withRouter() {
        case GET(p"/json") => Action { request =>
          val queries = request.queryString
          val locations = queries("locations").head
          val key = queries("key").head
          if(locations.equals(s"$lat,$long") && key.equals(Constants.googleApiKey)) {
            Results.Ok(Json.obj("results" ->
              Json.arr(Json.obj("elevation" -> elevationResult, "location" -> Json.obj("lat" -> lat, "lng" -> long), "resolution" -> SomeRandom.double()))))
          } else {
            Results.BadRequest("")
          }
        }
      } { implicit port =>
        WsTestClient.withClient { client =>
          val result = Await.result(new ElevationRetriever(client, "").retrieveElevation(pointLocation), 10.seconds)

          val expected = List(ElevationPoint(pointLocation.lat, pointLocation.long, elevationResult))
          result mustEqual expected
        }
      }
    }

    "call the google elevation API correctly for a LineLocation" in {
      val lineLocation = SomeRandom.lineLocation()
      testLocation(lineLocation)
    }

    "call the google elevation API correctly for a PolygonLocation" in {
      val polygonLocation = SomeRandom.polygonLocation()
      testLocation(polygonLocation)
    }

    def testLocation(location: Location) = {
      def getElevationPoints(points: Seq[(Double, Double)]): Array[ElevationPoint] = {
        points.map(p => ElevationPoint(p._1, p._2, SomeRandom.double())).toArray
      }

      def getPointsParam(points: Seq[(Double, Double)]): String = {
        points.map {
          case (lat, lng) => s"$lat,$lng"
        }.mkString("|")
      }

      def getElevationResult(points: Seq[(Double, Double)], elevationResults: Array[ElevationPoint]): Seq[JsObject] = {
        points.zipWithIndex.map {
          case ((lat, lng), index) => Json.obj("elevation" -> elevationResults(index).elevation, "location" -> Json.obj("lat" -> lat, "lng" -> lng), "resolution" -> SomeRandom.double())
        }
      }

      val elevationResults = location match {
        case l: LineLocation => getElevationPoints(l.points)
        case p: PolygonLocation => getElevationPoints(p.points)
      }
      val locationsInput = location match {
        case l: LineLocation => getPointsParam(l.points)
        case p: PolygonLocation => getPointsParam(p.points)
      }

      val results = location match {
        case l: LineLocation => getElevationResult(l.points, elevationResults)
        case p: PolygonLocation => getElevationResult(p.points, elevationResults)
      }

      Server.withRouter() {
        case GET(p"/json") => Action { request =>
          val queries = request.queryString
          val locations = queries("locations").head
          val key = queries("key").head
          if(locations.equals(locationsInput) && key.equals(Constants.googleApiKey)) {
            Results.Ok(Json.obj("results" ->  results))
          } else {
            Results.BadRequest("")
          }
        }
      } { implicit port =>
        WsTestClient.withClient { client =>
          val result = Await.result(new ElevationRetriever(client, "").retrieveElevation(location), 10.seconds)

          result mustEqual elevationResults
        }
      }
    }
  }

}
