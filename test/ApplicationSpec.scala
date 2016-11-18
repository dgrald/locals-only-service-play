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
//  "HomeController" should {
//
//    "render the index page" in {
//      val home = route(app, FakeRequest(GET, "/")).get
//
//      status(home) mustBe OK
//      contentType(home) mustBe Some("text/html")
//      contentAsString(home) must include ("Your new application is ready.")
//    }
//
//  }
//
//  "CountController" should {
//
//    "return an increasing count" in {
//      contentAsString(route(app, FakeRequest(GET, "/count")).get) mustBe "0"
//      contentAsString(route(app, FakeRequest(GET, "/count")).get) mustBe "1"
//      contentAsString(route(app, FakeRequest(GET, "/count")).get) mustBe "2"
//    }
//
//  }

}
