package models.daos.slick

import models.authorizations.Role
import models.daos.PostDAO
import models.posts.{PostRequest, AccessLevel, Blog, Post}
import models.users.User
import models.daos.slick.DBTableDefinitions._
import play.api.db.slick._
import play.api.db.slick.Config.driver.simple._


import scala.concurrent.Future



class PostDAOSlick extends PostDAO {

  import play.api.Play.current

  val PER_PAGE = 20

  override def fetch(id: String): Future[Option[Post]] = {
    DB withSession { implicit session =>
      Future.successful {
        slickPosts.filter(_.id === id).firstOption.map {
          p => Post(p.id, p.text, p.measurement)
        }
      }
    }
  }

  override def list(user: User, page: Int = 0): Future[Seq[Post]] = {
    DB withSession { implicit session =>
      Future.successful {
        slickPosts.filter(p => p.userID === user.id).drop(page * PER_PAGE).take(PER_PAGE).run.map(post =>
          Post(post.id, post.text, post.measurement)
        )
      }

    }

  }

  override def save(post: PostRequest, user: User): Future[Either[String, PostRequest]] = {
    DB withSession { implicit session =>
      Future.successful {
        val blog = post.blog
        slickBlogs.filter(_.id === post.blog).firstOption match {
          case Some(blog) =>
            Right {
              slickPosts += DBPost(post.id, user.id, post.text, post.measurement, blog.id, post.accessLevel) // TODO let's handle database exceptions?
              post
            }
          case None => Left(s"Blog with id $blog doesn't exist")
        }
      }
    }
  }
}
