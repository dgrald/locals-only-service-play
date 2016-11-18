package services

import play.api.libs.json._

/**
  * Created by dylangrald on 11/16/16.
  */
trait Location

case class PointLocation(lat: Double, long: Double) extends Location

case class Stash(name: String, location: Location)

object Location {

  implicit val locationReads: Reads[Location] = new Reads[Location] {
    override def reads(json: JsValue): JsResult[Location] = {
      (json \ "geometry" \ "type").validate[String] match {
        case JsSuccess(value, _) => value match {
          case "Point" =>
            val coordinates = (json \ "geometry" \ "coordinates").validate[List[Double]].get
            coordinates match {
              case List(lat, long) => JsSuccess(PointLocation(lat, long))
              case _ => JsError("Improper coordinates for point location")
            }
        }
      }
    }
  }

  implicit val locationWrites: Writes[Location] = new Writes[Location] {
    override def writes(o: Location): JsValue = o match {
      case PointLocation(lat, long) => Json.obj("type" -> "Feature",
          "geometry" -> Json.obj("type" -> "Point", "coordinates" -> Json.toJson[List[Double]](List(lat, long))))
    }
  }
}

object Stash {

  implicit val stashReads: Reads[Stash] = Json.reads[Stash]

  implicit val stashWrites: Writes[Stash] = Json.writes[Stash]

}