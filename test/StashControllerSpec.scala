import akka.stream.Materializer
import controllers.StashController
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.test._
import services.{Constants, PointLocation, StashStore}

import scala.concurrent.Future

/**
  * Created by dylangrald on 11/2/16.
  */
class StashControllerSpec extends PlaySpec with MockitoSugar with OneAppPerSuite with ScalaFutures {

  implicit lazy val materializer: Materializer = app.materializer

  def getPointLocation(jsValue: JsValue): PointLocation = {
    val coordinates = (jsValue \ "location" \ "geometry" \ "coordinates").get.validate[List[Double]].get
    PointLocation(coordinates.head, coordinates.last)
  }

  def setUpController(): (StashController, StashStore) = {
    val stashStore = mock[StashStore]
    val controller = new StashController(stashStore)
    (controller, stashStore)
  }

  "StashController.index" should {
    val allStashes = List(PointLocation(1.1, 1.1), PointLocation(2.2, 2.2))
    "return the stashes from the StashStore" in {
      val (controller, stashStore) = setUpController()
      when(stashStore.getStashes) thenReturn Future.successful(allStashes)

      val actual = controller.index(FakeRequest())

      val jsonValidation = contentAsJson(actual)
      val first = getPointLocation(jsonValidation(0).get)
      val second = getPointLocation(jsonValidation(1).get)
      List(first, second) mustEqual allStashes
    }
  }

  "StashController.addStash" should {
    val newStash = PointLocation(1.1, 1.1)
    "add a new stash to the StashStore" in {
      val (controller, stashStore) = setUpController()
      when(stashStore.addStash(newStash)) thenReturn Future.successful(newStash)
      val request = FakeRequest(POST, "/stash").withJsonBody(Json.parse(s"""{"location": {"type": "Feature", "geometry": {"type": "Point", "coordinates": [${newStash.lat}, ${newStash.long}]}}}"""))

      val actual = controller.addStash(request)

      val responseStash = getPointLocation(contentAsJson(actual))
      responseStash mustEqual newStash
    }

    "return bad request when given improper json" in {
      val (controller, _) = setUpController()
      val requestJson = s"""{"location": {"type": "Feature", "geometry": {"type": "Point", "coordinates": [${newStash.long}]}}}"""
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
