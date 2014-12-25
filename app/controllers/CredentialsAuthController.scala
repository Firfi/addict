package controllers

import javax.inject.Inject
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models.users.User
import play.api.Logger
import play.api.libs.json.{JsError, Json, JsSuccess}
import security.formatters.json.CredentialFormats
import security.models.Token

import scala.concurrent.Future
import play.api.mvc.Action
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.api.services.AuthInfoService
import com.mohiva.play.silhouette.api.exceptions.AuthenticationException
import models.services.{TokenServiceImpl, UserService}
import play.api.libs.concurrent.Execution.Implicits._

/**
 * The credentials auth controller.
 *
 * @param env The Silhouette environment.
 */
class CredentialsAuthController @Inject() (
  implicit val env: Environment[User, JWTAuthenticator],
  val userService: UserService,
  val authInfoService: AuthInfoService)
  extends Silhouette[User, JWTAuthenticator] {

  import CredentialFormats._
  /**
   * Authenticates a user against the credentials provider.
   *
   * @return The result to display.
   */
  def authenticate = Action.async(parse.json) { implicit request =>
    request.body.validate[Credentials] match {
      case JsSuccess(credentials, _) =>
        (env.providers.get(CredentialsProvider.ID) match {
          case Some(p: CredentialsProvider) => p.authenticate(credentials)
          case _ => Future.failed(new AuthenticationException(s"Cannot find credentials provider"))
        }).flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            case Some(user) => env.authenticatorService.create(loginInfo).flatMap { authenticator =>
              env.eventBus.publish(LoginEvent(user, request, request2lang))
              env.authenticatorService.init(authenticator, request).map { requestHeader =>
                val tok = TokenServiceImpl.tokenFromResponse(requestHeader)
                Ok(Json.toJson(Token(token = tok, expiresOn = authenticator.expirationDate)))
              }

            }
            case None =>
              Future.failed(new AuthenticationException("Couldn't find user"))
          }
        }.recoverWith(exceptionHandler)
      case JsError(e) => Future.successful(BadRequest(Json.obj("message" -> JsError.toFlatJson(e))))
    }
  }
}
