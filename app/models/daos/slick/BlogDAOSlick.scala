package models.daos.slick

import models.daos.{BlogDAO, PostDAO}
import models.posts._
import models.users.User
import models.daos.slick.DBTableDefinitions._
import play.api.db.slick._
import play.api.db.slick.Config.driver.simple._

import scala.concurrent.Future
import play.api.Play.current

class BlogDAOSlick extends BlogDAO {
  override def userBlogs(identifier: String): Future[Seq[Blog]] =
    DB withSession { implicit session =>
      Future.successful {
        val q = for {
          blog <- slickBlogs if blog.userID === identifier
          addiction <- slickAddictions if addiction.name === blog.addictionID
        } yield (blog.id, addiction.name)
        q.run.map({
          case (blogId, addictionName) => Blog(blogId, Addiction(addictionName))
        })
      }
    }

  override def save(blog: BlogRequest, user: User): Future[Either[String, BlogRequest]] = {
    DB withSession { implicit session =>
      val aid = blog.addictionId
      Future.successful {
        slickAddictions.filter(_.name === aid).firstOption match {
          case Some(addiction) =>
            slickBlogs += DBBlog(blog.id, user.id, addiction.name)
            Right(blog)
          case None => Left(s"can't find addiction $aid")
        }
      }
    }
  }

  override def retrieve(id: String): Future[Option[Blog]] = DB withSession { implicit session =>
    Future.successful {
      (for {
        blog <- slickBlogs if blog.id === id
        addiction <- slickAddictions if addiction.name === blog.addictionID
      } yield (blog.id, addiction.name)).firstOption.map { case (bid, name)=> Blog(bid, Addiction(name)) }
    }
  }
}
