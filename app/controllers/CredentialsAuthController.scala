package controllers

import javax.inject.Inject
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import formatters.json.AuthResultFormats
import models.users.{ProfileResult, AuthResult, User}
import play.api.Logger
import play.api.libs.json.{JsError, Json, JsSuccess}
import play.api.mvc.Results._
import security.formatters.json.CredentialFormats
import security.models.Token
import utils.responses.rest.Bad

import scala.concurrent.Future
import play.api.mvc.{Result, RequestHeader, Action}
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.api.services.AuthInfoService
import com.mohiva.play.silhouette.api.exceptions.{SilhouetteException, AccessDeniedException, AuthenticationException}
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
  import AuthResultFormats._

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
              Logger.warn(user.toString)
              env.eventBus.publish(LoginEvent(user, request, request2lang))
              env.authenticatorService.init(authenticator, request).map { requestHeader =>
                val tok = TokenServiceImpl.tokenFromResponse(requestHeader)
                Ok(Json.toJson(AuthResult(profile = ProfileResult(user = user),
                  token = Token(token = tok, expiresOn = authenticator.expirationDate))))
              }

            }
            case None =>
              Future.failed(new AuthenticationException("Couldn't find user"))
          }
        }.recoverWith {
          {
            case e: SilhouetteException => Future.successful(Unauthorized(Json.toJson(Bad(message = e.getMessage))))
          }
        }
      case JsError(e) => Future.successful(BadRequest(Json.obj("message" -> JsError.toFlatJson(e))))
    }
  }
}
