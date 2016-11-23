package controllers

import javax.inject.{Inject, Singleton}

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc
import play.api.mvc.{Action, AnyContent, Controller, Result}
import services.{Constants, JsonConverter, Stash, StashStore}

import scala.concurrent.Future

/**
  * Created by dylangrald on 11/2/16.
  */
@Singleton
class StashController @Inject()(stashStore: StashStore, jsonConverter: JsonConverter) extends Controller {

  def addStash = Action.async(request => {
    validateStashAndRespond(request, (stash) => stashStore.addStash(stash).map(addedStash => Ok(Json.toJson[Stash](addedStash))))
  })

  def index = Action.async(request => {
    stashStore.getStashes().map(stashes => Ok(Json.toJson(stashes)))
  })

  def getStash(id: String) = Action.async(request =>
    stashStore.getStash(id).map {
      case Some(retrievedStash) => Ok(Json.toJson[Stash](retrievedStash))
      case None => NotFound
    }
  )

  def updateStash = Action.async(request => {
    validateStashAndRespond(request, (stash) => stashStore.updateStash(stash).map(updatedStash => Ok(Json.toJson[Stash](updatedStash))))
  })

  private def validateStashAndRespond(request: mvc.Request[AnyContent], responseAction: (Stash) => Future[Result]): Future[Result] = {
    request.body.asJson match {
      case None => Future.successful(BadRequest(Constants.noValidJsonMessage))
      case Some(json) =>
        val stash = jsonConverter.getStashFromRequestBody(json)
        stash match {
          case Some(value) => responseAction(value)
          case None => Future.successful(BadRequest(json))
        }
    }
  }
}

