package controllers

import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, JsError, Json}
import utils.responses.rest.Bad
import play.api.mvc.Results._

import scala.concurrent.Future
import play.api.mvc._

object Utils {
  val jsonRecover: JsError => Future[Result] = { e =>
    Future.successful(BadRequest(Json.toJson(Bad(JsError.toFlatJson(e)))))
  }

  def userNotFoundHandler(identifier: String) = Future.successful(NotFound(Json.toJson(Bad(message = s"User $identifier not found"))))

}
