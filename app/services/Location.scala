package services

import play.api.libs.json._

/**
  * Created by dylangrald on 11/16/16.
  */
trait Location

case class PointLocation(lat: Double, long: Double) extends Location

object Location {

  implicit val locationReads = new Reads[Location] {
    override def reads(json: JsValue): JsResult[Location] = {
      (json \ "location" \ "geometry" \ "type").validate[String] match {
        case JsSuccess(value, _) => value match {
          case "Point" =>
            val coordinates = (json \ "location" \ "geometry" \ "coordinates").validate[List[Double]].get
            coordinates match {
              case List(lat, long) => JsSuccess(PointLocation(lat, long))
              case _ => JsError("Improper coordinates for point location")
            }
        }
      }
    }
  }

  implicit val locationWrites = new Writes[Location] {
    override def writes(o: Location): JsValue = o match {
      case PointLocation(lat, long) => Json.obj("location" ->
        Json.obj("type" -> "Feature",
          "geometry" -> Json.obj("type" -> "Point", "coordinates" -> Json.toJson[List[Double]](List(lat, long)))))
    }
  }
}
