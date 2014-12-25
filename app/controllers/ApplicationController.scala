package controllers

import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.api.{LogoutEvent, Environment, Silhouette}
import com.mohiva.play.silhouette.api
import models.users.User
import play.api.Logger
import play.api.libs.json.Json
import utils.responses.rest.Good
import scala.concurrent.Future
import javax.inject.Inject

/**
 * The basic application controller.
 *
 * @param env The Silhouette environment.
 */
class ApplicationController @Inject() (implicit val env: Environment[User, JWTAuthenticator])
  extends Silhouette[User, JWTAuthenticator] {

  implicit val userFormat = formatters.json.UserFormats.restFormat

  /**
   * Handles the index action.
   *
   * @return The result to display.
   */
  def index = SecuredAction.async { implicit request =>
      Future.successful(Ok(Json.toJson(request.identity)))
  }

}
