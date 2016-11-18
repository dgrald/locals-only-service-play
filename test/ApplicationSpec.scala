import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import org.scalatest.mock.MockitoSugar

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends PlaySpec with OneAppPerTest with MockitoSugar {

  "Routes" should {

    "send 404 on a bad request" in  {
      route(app, FakeRequest(GET, "/boum")).map(status(_)) mustBe Some(NOT_FOUND)
    }

  }

//  "StashController" should {
//    "render the index page" in {
//      val stashes = route(app, FakeRequest(GET, "/stash")).get
//
//      status(stashes) mustBe OK
//      contentType(stashes) mustBe Some("application/json")
//      contentAsString(stashes) must include ("Blah")
//    }
//  }
//

}
