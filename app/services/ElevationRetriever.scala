package services

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import com.google.inject.Inject
import play.api.libs.ws.WSClient

import scala.concurrent.Future

/**
  * Created by dylangrald on 12/29/16.
  */
class ElevationRetriever(ws: WSClient, baseUrl: String) {
  @Inject def this(ws: WSClient) = this(ws, "https://maps.googleapis.com/maps/api/elevation")

  def retrieveElevation(lat: Double, long: Double): Future[Double] = {
    val locationQueryString = ("locations", s"$lat,$long")
    val keyQueryString = ("key", Constants.googleApiKey)
    ws.url(baseUrl + "/json").withQueryString(locationQueryString, keyQueryString).get().map { response =>
      val resultsJson = response.json
      val results = resultsJson \ "results"
      val elevation = results(0) \ "elevation"
      elevation.validate[Double].get
    }
  }

}
