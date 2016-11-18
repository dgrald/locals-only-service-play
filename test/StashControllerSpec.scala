import akka.stream.Materializer
import controllers.StashController
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.test._
import services.{Stash, Constants, PointLocation, StashStore}

import scala.concurrent.Future

/**
  * Created by dylangrald on 11/2/16.
  */
class StashControllerSpec extends PlaySpec with MockitoSugar with OneAppPerSuite with ScalaFutures {

  implicit lazy val materializer: Materializer = app.materializer

  def getPointStash(jsValue: JsValue): Stash = {
    val coordinates = (jsValue \ "location" \ "geometry" \ "coordinates").get.validate[List[Double]].get
    val pointLocation = PointLocation(coordinates.head, coordinates.last)
    val name = (jsValue \ "name").get.validate[String].get
    Stash(name, pointLocation)
  }

  def setUpController(): (StashController, StashStore) = {
    val stashStore = mock[StashStore]
    val controller = new StashController(stashStore)
    (controller, stashStore)
  }

  "StashController.index" should {
    val allStashes = List(SomeRandom.pointLocationStash(), SomeRandom.pointLocationStash())
    "return the stashes from the StashStore" in {
      val (controller, stashStore) = setUpController()
      when(stashStore.getStashes) thenReturn Future.successful(allStashes)

      val actual = controller.index(FakeRequest())

      val jsonValidation = contentAsJson(actual)
      val first = getPointStash(jsonValidation(0).get)
      val second = getPointStash(jsonValidation(1).get)
      List(first, second) mustEqual allStashes
    }
  }

  "StashController.addStash" should {
    val newStashPointLocation = SomeRandom.pointLocation()
    val newStash = SomeRandom.pointLocationStash(newStashPointLocation)
    "add a new stash to the StashStore" in {
      val (controller, stashStore) = setUpController()
      when(stashStore.addStash(newStash)) thenReturn Future.successful(newStash)
      val request = FakeRequest(POST, "/stash").withJsonBody(
        Json.parse(s"""{"name": "${newStash.name}", "location": {"type": "Feature", "geometry": {"type": "Point", "coordinates": [${newStashPointLocation.lat}, ${newStashPointLocation.long}]}}}""".stripMargin))

      val actual = controller.addStash(request)

      val responseStash = getPointStash(contentAsJson(actual))
      responseStash mustEqual newStash
    }

    "return bad request when given improper point location json" in {
      val (controller, _) = setUpController()
      val requestJson = s"""{"name": "${SomeRandom.string()}", "location": {"type": "Feature", "geometry": {"type": "Point", "coordinates": []}}}"""
      val request = FakeRequest(POST, "/stash").withJsonBody(Json.parse(requestJson))

      val actual = controller.addStash(request)

      status(actual) mustBe BAD_REQUEST
      contentAsString(actual) mustEqual requestJson.replaceAll("\\s", "")
    }

    "return bad request when given no json" in {
      val (controller, _) = setUpController()
      val request = FakeRequest(POST, "/stash").withTextBody("")

      val actual = controller.addStash(request)

      status(actual) mustBe BAD_REQUEST
      contentAsString(actual) mustEqual Constants.noValidJsonMessage
    }
  }
}
