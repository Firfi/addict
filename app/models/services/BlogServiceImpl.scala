package models.services

import javax.inject.Inject

import models.daos.{BlogDAO, PostDAO}
import models.posts.{BlogRequest, Blog}
import models.users.User

import scala.concurrent.Future

class BlogServiceImpl @Inject() (blogDAO: BlogDAO) extends BlogService {
  override def userBlogs(identifier: String): Future[Seq[Blog]] = blogDAO.userBlogs(identifier)

  override def retrieve(id: String): Future[Option[Blog]] = blogDAO.retrieve(id)

  override def save(blog: BlogRequest, user: User): Future[Either[String, BlogRequest]] = blogDAO.save(blog, user)
}
