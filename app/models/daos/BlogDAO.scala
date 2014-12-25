package models.daos

import models.posts.{BlogRequest, Blog}
import models.users.User

import scala.concurrent.Future

trait BlogDAO {
  def save(blog: BlogRequest, user: User): Future[Either[String, BlogRequest]]

  def retrieve(id: String): Future[Option[Blog]]

  def userBlogs(identifier: String): Future[Seq[Blog]]
}
