package models.services

import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.api.services.{AuthInfo, IdentityService}
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.authorizations.Role
import models.posts.{PostRequest, AccessLevel, Blog, Post}
import models.users.User
import scala.concurrent.Future

trait PostService {

  def list(user: User): Future[Seq[Post]]

  def save(post: PostRequest, user: User): Future[Either[String, PostRequest]]

  def fetch(id: String): Future[Option[Post]]

}
