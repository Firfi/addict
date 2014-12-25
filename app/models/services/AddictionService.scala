package models.services

import models.posts.Addiction

import scala.concurrent.Future

trait AddictionService {
  def fetch(s: String): Future[Option[Addiction]]

  def init(): Future[Stream[Addiction]] // init addictions that we have
}
