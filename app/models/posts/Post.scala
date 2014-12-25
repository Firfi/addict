package models.posts

import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import models.authorizations.{SimpleUser, Role}
import models.users.User

object Post {
  def genId = UUID.randomUUID.toString
}


case class Post(id: String = Post.genId,
                text: Option[String],
                measurement: Option[Long])

case class PostRequest(id: String = Post.genId,
                       text: Option[String] = None,
                       measurement: Option[Long]= None,
                       accessLevel: Int = 0,
                       blog: String)
