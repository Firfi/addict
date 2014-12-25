package controllers

import javax.inject.Inject
import com.mohiva.play.silhouette.api.exceptions.AuthenticationException
import com.mohiva.play.silhouette.api.{LoginEvent, Silhouette, Environment}
import com.mohiva.play.silhouette.api.services.AuthInfoService
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfileBuilder, SocialProvider}
import models.users.User
import play.api.libs.json.Json
import security.models.Token

import scala.concurrent.Future
import play.api.mvc.Action
import play.api.libs.concurrent.Execution.Implicits._
import models.services.{TokenServiceImpl, UserService}

/**
 * The social auth controller.
 *
 * @param env The Silhouette environment.
 */
class SocialAuthController @Inject() (
                                       val env: Environment[User, JWTAuthenticator],
                                       val userService: UserService,
                                       val authInfoService: AuthInfoService) extends Silhouette[User, JWTAuthenticator] {

  /**
   * Authenticates a user against a social provider.
   *
   * @param provider The ID of the provider to authenticate against.
   * @return The result to display.
   */
  def authenticate(provider: String) = Action.async { implicit request =>
    (env.providers.get(provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            user <- userService.save(profile)
            authInfo <- authInfoService.save(profile.loginInfo, authInfo)
            authenticator <- env.authenticatorService.create(user.loginInfo)
            requestHeader <- env.authenticatorService.init(authenticator, request)
          } yield {
            env.eventBus.publish(LoginEvent(user, request, request2lang))
            Ok(Json.toJson(Token(token = TokenServiceImpl.tokenFromResponse(requestHeader), expiresOn = authenticator.expirationDate)))
          }
        }
      case _ => Future.failed(new AuthenticationException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recoverWith(exceptionHandler)
  }
}
