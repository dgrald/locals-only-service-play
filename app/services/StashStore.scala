package services

import javax.inject.Inject

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future

/**
  * Created by dylangrald on 11/2/16.
  */
class StashStore @Inject()(val reactiveMongoApi: ReactiveMongoApi) {

  implicit val locationFormat: OFormat[Location] = new OFormat[Location] {
    override def writes(o: Location): JsObject = Json.obj("loc" -> Location.locationWrites.writes(o))

    override def reads(json: JsValue): JsResult[Location] = Location.locationReads.reads((json \ "loc").get)
  }

  implicit val jsonFormat: OWrites[JsObject] = new OWrites[JsObject] {
    override def writes(o: JsObject): JsObject = o
  }

  val locationCollection = reactiveMongoApi.database.map(d => d.collection[JSONCollection]("locations"))

  def addStash(location: Location): Future[Location] = {
    locationCollection.flatMap(l => l.insert[Location](location).map(_ => location))
  }

  def getStashes(): Future[Seq[Location]] = {
    locationCollection.flatMap(l => l.find(Json.obj()).cursor[Location]().collect[List]())
  }
}