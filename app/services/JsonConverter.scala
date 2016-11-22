package services

import play.api.libs.json.{JsError, JsSuccess, JsValue}

/**
  * Created by dylangrald on 11/22/16.
  */
class JsonConverter {

  def getStashFromRequestBody(requestBody: JsValue): Option[Stash] = {
    requestBody.validate[Stash](Stash.stashRequestBodyReads) match {
      case JsSuccess(value, _) => Some(value)
    }
  }

}
