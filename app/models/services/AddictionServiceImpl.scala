package models.services

import com.google.inject.Inject
import models.daos.AddictionDAO
import models.posts.Addiction

import scala.concurrent.Future
import scala.util.Success
import scalaz.{Tree, TreeFunctions}
import scalaz.syntax.ToTreeOps
import scala.concurrent.ExecutionContext.Implicits.global

class AddictionServiceImpl @Inject() (addictionDAO: AddictionDAO) extends AddictionService with ToTreeOps with TreeFunctions {

  override def fetch(id: String): Future[Option[Addiction]] = {
    addictionDAO.retrieve(id)
  }

  // Initialize addictions with parents in db in the way when parents are initialized first. TODO we can generalize it further
  override def init(): Future[Stream[Addiction]] = {
    def add(t: Tree[String], parent: Option[Tree[String]] = None): Future[Stream[Addiction]] = {
      val currentAddiction = Addiction(t.rootLabel)
      addictionDAO.retrieve(currentAddiction.name).map {
        case Some(addiction) => Future.successful(Success(currentAddiction)) // exist already count it as created
        case None =>  // our case
          addictionDAO.save(currentAddiction)
      }.flatMap { _ =>
        Future.sequence {
          t.subForest map { subTree =>
            add(subTree, Some(t))
          }
        }
      }.map(_.flatten)
    }
    Future.sequence {
      addictions.map { addiction =>
        add(addiction, None) // map roots
      }
    }.map(_.flatten)
  }

  val addictions = "".node(
    "Alcohol".node(
      "Beer".leaf,
      "Vodka".leaf
    ),
    "Drugs".node(
      "Cocain".leaf
    )
  ).subForest

}
