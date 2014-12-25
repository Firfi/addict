package models.daos.slick

import models.daos.AddictionDAO
import models.posts.Addiction

import scala.concurrent.Future
import models.daos.slick.DBTableDefinitions.{DBAddiction, slickAddictions}

import play.api.db.slick._
import play.api.db.slick.Config.driver.simple._

import play.api.Play.current

import scala.util.Try

class AddictionDAOSlick extends AddictionDAO {
  override def retrieve(name: String): Future[Option[Addiction]] = {
    DB withSession { implicit session =>
      Future.successful {
        slickAddictions.filter(_.name === name).firstOption.map(a => Addiction(a.name))
      }
    }
  }

  override def save(addiction: Addiction): Future[Try[Addiction]] = {
    DB withSession { implicit session =>
      Future.successful {
        Try {
          slickAddictions += DBAddiction(addiction.name)
          addiction
        }
      }
    }
  }
}
