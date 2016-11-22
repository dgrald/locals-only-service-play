import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import services.{JsonConverter, LineLocation, Location, PolygonLocation}

/**
  * Created by dylangrald on 11/22/16.
  */
class JsonConverterSpec extends PlaySpec {

  val jsonConverter = new JsonConverter()

  "getStashFromRequestBody" should {
    "parse a PointLocation and" should {
      "return a new PointLocation with the correct values" in {
        val newStashPointLocation = SomeRandom.pointLocation()
        val newStash = SomeRandom.stash(newStashPointLocation)
        val input = Json.parse(s"""{"name": "${newStash.name}", "location": {"type": "Feature", "geometry": {"type": "Point", "coordinates": [${newStashPointLocation.lat}, ${newStashPointLocation.long}]}}}""".stripMargin)

        val actual = jsonConverter.getStashFromRequestBody(input).get

        actual.name mustEqual newStash.name
        actual.location mustEqual newStash.location
      }
    }

    "parse a LineLocation and" should {
      "return a new LineLocation with the correct values" in {
        val lineLocation = SomeRandom.lineLocation()
        val newStash = SomeRandom.stash(lineLocation)
        val requestJson = Json.parse(
          s"""{"name": "${newStash.name}",
              | "location": {"type": "Feature",
              | "geometry": {"type": "LineString",
              |  "coordinates": [${convertPointCoordinatesToJsonString(lineLocation)}]}}}""".stripMargin
        )

        val actual = jsonConverter.getStashFromRequestBody(requestJson).get

        actual.name mustEqual newStash.name
        actual.location mustEqual newStash.location
      }
    }

    "parse a PolygonLocation and" should {
      "return a new PolygonCLocation with the correct values" in {
        val polygonLocation = SomeRandom.polygonLocation()
        val newStash = SomeRandom.stash(polygonLocation)
        val requestJson = Json.parse(
          s"""{"name": "${newStash.name}",
              | "location": {"type": "Feature",
              |  "geometry": {"type": "Polygon",
              |   "coordinates": [${convertPointCoordinatesToJsonString(polygonLocation)}]}}}""".stripMargin
        )

        val actual = jsonConverter.getStashFromRequestBody(requestJson).get

        actual.name mustEqual newStash.name
        actual.location mustEqual newStash.location
      }
    }
  }

  def mapCoordinates(coordinates: List[(Double, Double)]): String = {
    coordinates.map(p => s"[${p._1}, ${p._2}]").mkString(",")
  }

  def convertPointCoordinatesToJsonString(location: Location): String = location match {
    case line: LineLocation => mapCoordinates(line.points)
    case polygonLocation: PolygonLocation => mapCoordinates(polygonLocation.points)
  }
}
