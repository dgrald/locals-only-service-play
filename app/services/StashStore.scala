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

  implicit val locationFormat: OFormat[Stash] = new OFormat[Stash] {
    override def writes(o: Stash): JsObject = Json.obj("_id" -> o._id, "stash" -> Stash.stashWrites.writes(o))

    override def reads(json: JsValue): JsResult[Stash] = Stash.stashReads.reads((json \ "stash").get)
  }

  implicit val jsonFormat: OWrites[JsObject] = new OWrites[JsObject] {
    override def writes(o: JsObject): JsObject = o
  }

  val locationCollection = reactiveMongoApi.database.map(d => d.collection[JSONCollection]("stashes"))

  def addStash(stash: Stash): Future[Stash] = {
    locationCollection.flatMap(l => l.insert[Stash](stash).map(_ => stash))
  }

  def getStashes(): Future[Seq[Stash]] = {
    locationCollection.flatMap(l => l.find(Json.obj()).cursor[Stash]().collect[List]())
  }

  def getStash(id: String): Future[Option[Stash]] = {
    locationCollection.flatMap(l => l.find(Json.obj("_id" -> id)).cursor[Stash].collect[List]().map {
      case List(matchingStash) => Some(matchingStash)
      case _ => None
    })
  }

  def deleteStash(id: String): Future[Boolean] = {
    locationCollection.flatMap(l => l.findAndRemove(Json.obj("_id" -> id)).map(result => true))
  }

  def updateStash(updatedStash: Stash): Future[Stash] = {
    locationCollection.flatMap(l => l.findAndUpdate(Json.obj("_id" -> updatedStash._id), updatedStash).map(_ => updatedStash))
  }
}