package services

import java.util.UUID

import play.api.libs.json._

/**
  * Created by dylangrald on 11/16/16.
  */
trait Location

case class PointLocation(lat: Double, long: Double) extends Location

case class LineLocation(points: List[(Double, Double)]) extends Location

case class PolygonLocation(points: List[(Double, Double)]) extends Location

case class Stash(_id: String, name: String, location: Location)

object Location {

  implicit val locationReads: Reads[Location] = new Reads[Location] {

    def convertCoordinatesToPairs(coordinates: List[List[Double]]): List[(Double, Double)] = {
      coordinates.map(c => (c.head, c.last))
    }

    def getPointsJsValue(json: JsValue): List[List[Double]] = {
      (json \ "geometry" \ "coordinates").validate[List[List[Double]]].get
    }

    override def reads(json: JsValue): JsResult[Location] = {
      (json \ "geometry" \ "type").validate[String] match {
        case JsSuccess(value, _) => value match {
          case "Point" =>
            val coordinates = (json \ "geometry" \ "coordinates").validate[List[Double]].get
            coordinates match {
              case List(lat, long) => JsSuccess(PointLocation(lat, long))
              case _ => JsError("Improper coordinates for point location")
            }
          case "LineString" =>
            val coordinates = getPointsJsValue(json)
            JsSuccess(LineLocation(convertCoordinatesToPairs(coordinates)))
          case "Polygon" =>
            val coordinates = getPointsJsValue(json)
            JsSuccess(PolygonLocation(convertCoordinatesToPairs(coordinates)))
        }
      }
    }
  }

  implicit val locationWrites: Writes[Location] = new Writes[Location] {

    def convertPairsToCoordinates(pairs: List[(Double, Double)]): List[List[Double]] = {
      pairs.map(p => List(p._1, p._2))
    }

    def getPointsJsValue(points: List[(Double, Double)]): JsValue = {
      Json.toJson[List[List[Double]]](convertPairsToCoordinates(points))
    }

    override def writes(o: Location): JsValue = o match {
      case PointLocation(lat, long) => Json.obj("type" -> "Feature",
          "geometry" -> Json.obj("type" -> "Point", "coordinates" -> Json.toJson[List[Double]](List(lat, long))))
      case LineLocation(points) =>
        val pointsJson = getPointsJsValue(points)
        Json.obj("type" -> "Feature", "geometry" -> Json.obj("type" -> "LineString", "coordinates" -> pointsJson))
      case PolygonLocation(points) =>
        val pointsJson = getPointsJsValue(points)
        Json.obj("type" -> "Feature", "geometry" -> Json.obj("type" -> "Polygon", "coordinates" -> pointsJson))
    }
  }
}

object Stash {

  implicit val stashRequestBodyReads = new Reads[Stash] {
    override def reads(json: JsValue): JsResult[Stash] = {
      val location = (json \ "location").validate[Location].get
      val name = (json \ "name").validate[String].get
      val id = UUID.randomUUID().toString
      JsSuccess(new Stash(id, name, location))
    }
  }

  implicit val stashReads: Reads[Stash] = Json.reads[Stash]

  implicit val stashWrites: Writes[Stash] = Json.writes[Stash]

}