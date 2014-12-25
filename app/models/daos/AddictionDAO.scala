package models.daos

import models.posts.{Addiction, Blog}

import scala.concurrent.Future
import scala.util.Try

trait AddictionDAO {
  def retrieve(name: String): Future[Option[Addiction]]
  def save(addiction: Addiction): Future[Try[Addiction]]
}
