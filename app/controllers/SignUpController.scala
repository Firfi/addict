package controllers

import java.util.UUID
import javax.inject.Inject
import _root_.security.models.SignUp
import _root_.security.models.Token
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.api.LoginEvent
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.SignUpEvent
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models.users.User
import play.api.Logger
import Utils._
import play.api.libs.json.{JsError, Json, JsSuccess}
import security.formatters.json.CredentialFormats
import utils.responses.rest.Bad

import scala.concurrent.Future
import play.api.mvc.Action
import play.api.libs.concurrent.Execution.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.services.{AvatarService, AuthInfoService}
import models.services.{TokenServiceImpl, UserService}

/**
 * The sign up controller.
 *
 * @param env The Silhouette environment.
 * @param userService The user service implementation.
 * @param authInfoService The auth info service implementation.
 * @param avatarService The avatar service implementation.
 * @param passwordHasher The password hasher implementation.
 */
class SignUpController @Inject() (
                                   implicit val env: Environment[User, JWTAuthenticator],
                                   val userService: UserService,
                                   val authInfoService: AuthInfoService,
                                   val avatarService: AvatarService,
                                   val passwordHasher: PasswordHasher) extends Silhouette[User, JWTAuthenticator] {

  import CredentialFormats._

  /**
   * Registers a new user.
   *
   * @return The result to display.
   */
  def signUp = Action.async(parse.json) { implicit request =>
    request.body.validate[SignUp].map (

      signUp => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, signUp.identifier)
        userService.retrieve(loginInfo).map {
          case None => /* user not already exists */
            val authInfo = passwordHasher.hash(signUp.password)
            val user =  User(
              loginInfo = loginInfo,
              email = Some(signUp.identifier),
              name = signUp.name,
              avatarURL = None
            )
            for {
              avatar <- avatarService.retrieveURL(signUp.identifier)
              user <- userService.save(user.copy(avatarURL = avatar))
              authInfo <- authInfoService.save(loginInfo, authInfo)
              authenticator <- env.authenticatorService.create(loginInfo)
              requestHeader <- env.authenticatorService.init(authenticator, request)
            } yield {
              env.eventBus.publish(SignUpEvent(user, request, request2lang))
              env.eventBus.publish(LoginEvent(user, request, request2lang))
              Ok(Json.toJson(Token(token = TokenServiceImpl.tokenFromResponse(requestHeader), expiresOn = authenticator.expirationDate)))
            }
          case Some(u) => /* user already exists! */
            Future.successful(Conflict(Json.toJson(Bad(message = "user already exists"))))
        }.flatMap { r => r }
      }
    ).recoverTotal(jsonRecover)
  }
}
