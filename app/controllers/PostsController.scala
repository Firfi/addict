package controllers

import javax.inject.Inject
import com.mohiva.play.silhouette.api.{LoginEvent, Silhouette, Environment}
import com.mohiva.play.silhouette.api.services.AuthInfoService
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models.posts.{PostRequest, Post}
import models.users.User
import play.api.libs.json.Json
import security.models.Token
import utils.responses.rest.{Good, Bad}

import scala.concurrent.Future
import play.api.mvc.Action
import play.api.libs.concurrent.Execution.Implicits._
import models.services.{PostService, TokenServiceImpl, UserService}

import Utils._

/**
 * The social auth controller.
 *
 * @param env The Silhouette environment.
 */
class PostsController @Inject() (
                                       val env: Environment[User, JWTAuthenticator],
                                       val userService: UserService,
                                       val postService: PostService,
                                       val authInfoService: AuthInfoService) extends Silhouette[User, JWTAuthenticator] {

  implicit val postsFormat = formatters.json.PostFormats.restFormat
  implicit val postsRequestFormat = formatters.json.PostFormats.requestRestFormat
  implicit val userFormat = formatters.json.UserFormats.restFormat
  implicit val goodFormat = utils.responses.rest.Good.restFormat

  def userPosts(identifier: String) = UserAwareAction.async { implicit request =>
    (for {
      u <- userService.retrieve(identifier)
    } yield {
      u match {
        case Some(user) => postService.list(user).map(l => Ok(Json.toJson(Good(Json.toJson(l)))))
        case None => userNotFoundHandler(identifier)
      }
    }).flatMap(a => a)
  }

  def create = SecuredAction.async(parse.json) { implicit request =>
    val user = request.identity
    request.body.validate[PostRequest].map (
      postRequest => {
        postService.save(postRequest, user).map {
          case Right(post) => Ok(Json.toJson(Good(Json.toJson(post))))
          case Left(error) => BadRequest(Json.toJson(Bad(Json.toJson(error))))
        }
      }
    ).recoverTotal(jsonRecover)

  }

//  def userBlogs(identifier: String) = Action.async { implicit request =>
//    (for {
//      u <- userService.retrieve(identifier)
//    } yield {
//      u match {
//        case Some(user) => {
//
//        }
//        case None => userNotFoundHandler(identifier)
//      }
//    }).flatMap(a => a)
//  }

}
