package controllers

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import controllers.Utils._
import formatters.json.BlogFormats
import models.posts.{BlogRequest, PostRequest}
import models.services.{UserService, BlogService}
import models.users.User
import play.api.libs.json.Json
import play.api.mvc.BodyParsers.parse
import scala.concurrent.ExecutionContext.Implicits.global
import utils.responses.rest.{Bad, Good}

import com.mohiva.play.silhouette.test._

import scala.concurrent.Future


class BlogsController @Inject() (val blogService: BlogService,
                                 val userService: UserService,
                                 val env: Environment[User, JWTAuthenticator])
  extends Silhouette[User, JWTAuthenticator] {

  implicit val requestBlogFormats = BlogFormats.requestBlogFormats
  implicit val blogFormats = BlogFormats.blogFormats

  def userBlogs(identifier: String) = UserAwareAction.async { implicit request =>
    userService.retrieve(identifier).flatMap {
      case Some(user) => blogService.userBlogs(user.id).map { blogs =>
        Ok(Json.toJson(Good(Json.toJson(blogs))))
      }
      case None => Future.successful {
        BadRequest(Json.toJson(Bad(message = s"there's no user with identifier: $identifier}")))
      }
    }
  }

  def create = SecuredAction.async(parse.json) { implicit request =>
    val user = request.identity
    request.body.validate[BlogRequest].map (
      blogRequest => {
        blogService.save(blogRequest, user).map {
          case Right(blog) => Ok(Json.toJson(Good(Json.toJson(blog))))
          case Left(error) => BadRequest(Json.toJson(Bad(Json.toJson(error))))
        }
      }
    ).recoverTotal(jsonRecover)
  }
}
