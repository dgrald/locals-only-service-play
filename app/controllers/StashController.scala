package controllers

import javax.inject.{Inject, Singleton}

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services.{JsonConverter, Constants, Stash, StashStore}

import scala.concurrent.Future

/**
  * Created by dylangrald on 11/2/16.
  */
@Singleton
class StashController @Inject()(stashStore: StashStore, jsonConverter: JsonConverter) extends Controller {

  def addStash = Action.async(request => {
    request.body.asJson match {
      case None => Future.successful(BadRequest(Constants.noValidJsonMessage))
      case Some(json) =>
        val stash = jsonConverter.getStashFromRequestBody(json)
        stash match {
          case Some(value) => stashStore.addStash(value).map(addedStash => Ok(Json.toJson[Stash](addedStash)))
          case None => Future.successful(BadRequest(json))
        }
    }
  })

  def index = Action.async(request => {
    stashStore.getStashes().map(stashes => Ok(Json.toJson(stashes)))
  })
}

