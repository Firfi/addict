package models.daos

import scala.concurrent.Future
import models.posts.{PostRequest, AccessLevel, Blog, Post}
import models.users.User

trait PostDAO {
  def fetch(id: String): Future[Option[Post]]

  def list(user: User, page: Int = 0): Future[Seq[Post]]
  def save(post: PostRequest, user: User): Future[Either[String, PostRequest]]
}
