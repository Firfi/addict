package models.services

import models.posts.{BlogRequest, Blog}
import models.users.User

import scala.concurrent.Future

trait BlogService {
  def userBlogs(identifier: String): Future[Seq[Blog]]
  def retrieve(id: String): Future[Option[Blog]]
  def save(blog: BlogRequest, user: User): Future[Either[String, BlogRequest]]
}
