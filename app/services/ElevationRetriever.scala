package services

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import com.google.inject.Inject
import play.api.libs.json.{Json, Reads, Writes}
import play.api.libs.ws.WSClient

import scala.concurrent.Future

/**
  * Created by dylangrald on 12/29/16.
  */
class ElevationRetriever(ws: WSClient, baseUrl: String) {
  @Inject def this(ws: WSClient) = this(ws, "https://maps.googleapis.com/maps/api/elevation")

  implicit val elevationResultReads: Reads[ElevationResult] = ElevationResult.elevationResultReads

  def retrieveElevation(location: Location): Future[Seq[ElevationPoint]] = {

    def getLocationQueryString(points: Seq[(Double, Double)]): String = {
      points.map {
        case (lat, long) => s"$lat,$long"
      }.mkString("|")
    }

    val locationQueryStringValue = location match {
      case point: PointLocation => s"${point.lat},${point.long}"
      case line: LineLocation => getLocationQueryString(line.points)
      case polygon: PolygonLocation => getLocationQueryString(polygon.points)
    }

    val locationQueryString = ("locations", locationQueryStringValue)
    val keyQueryString = ("key", Constants.googleApiKey)
    ws.url(baseUrl + "/json").withQueryString(locationQueryString, keyQueryString).get().map { response =>
      val resultsJson = response.json
      val results = resultsJson \ "results"
      println(results)
      val elevationResults = results.validate[List[ElevationResult]].get

      def mapPointsToElevations(points: Seq[(Double, Double)]): Seq[ElevationPoint] = {
        points.map {
          case (lat, long) =>
            val matchingPoint = elevationResults.find(r => r.location.lat == lat && r.location.lng == long)
            ElevationPoint(lat, long, matchingPoint.get.elevation)
        }
      }

      location match {
        case point: PointLocation => List(ElevationPoint(point.lat, point.long, elevationResults.head.elevation))
        case line: LineLocation => mapPointsToElevations(line.points)
        case polygon: PolygonLocation => mapPointsToElevations(polygon.points)
      }
    }
  }

}

case class ElevationPoint(lat: Double, lng: Double, elevation: Double)

case class ElevationResult(elevation: Double, location: ElevationResultLocation, resolution: Double)

case class ElevationResultLocation(lat: Double, lng: Double)

object ElevationResult {
  def elevationResultLocationReads: Reads[ElevationResultLocation] = Json.reads[ElevationResultLocation]

  implicit val locationReads = elevationResultLocationReads

  def elevationResultReads: Reads[ElevationResult] = Json.reads[ElevationResult]
}
