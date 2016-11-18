package controllers

import javax.inject.{Inject, Singleton}

import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services.{ Location, StashStore}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
  * Created by dylangrald on 11/2/16.
  */
@Singleton
class StashController @Inject()(stashStore: StashStore) extends Controller {

  def addStash = Action.async(request => {
    val json = request.body.asJson.get
    val location = json.validate[Location]
    location match {
      case JsSuccess(value, _) => stashStore.addStash(value).map(addedLocation => Ok(Json.toJson[Location](addedLocation)))
      case JsError(error) => Future.successful(BadRequest(json))
    }
  })

  def index = Action.async(request => {
    stashStore.getStashes().map(stashes => Ok(Json.toJson(stashes)))
  })
}

