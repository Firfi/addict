package models.services

import javax.inject.Inject

import models.daos.{PostDAO}
import models.posts.{PostRequest, AccessLevel, Blog, Post}
import models.users.User

import scala.concurrent.Future
import scalaz._
import Scalaz._

object AccessLevelTools {
  def levels = Map(
    0->"Everyone",
    1->"Anonymous"
  )
  def validate(accessLevel: Int): Validation[String, String] = {
    levels.get(accessLevel) match {
      case Some(_) => "ok".success
      case None => s"No such access level: $accessLevel".failure
    }
  }
}


class PostServiceImpl @Inject() (postDAO: PostDAO, blogService: BlogService) extends PostService{
  override def list(user: User): Future[Seq[Post]] = postDAO.list(user)

  override def save(post: PostRequest, user: User): Future[Either[String, PostRequest]] = {
    AccessLevelTools.validate(post.accessLevel) match {
      case Success(_) => postDAO.save(post, user)
      case Failure(m) => Future.successful(Left(m))
    }

  }

  override def fetch(id: String): Future[Option[Post]] = {
    postDAO.fetch(id)
  }
}
