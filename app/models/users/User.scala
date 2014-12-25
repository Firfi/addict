package models.users

import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import models.authorizations.{Role, SimpleUser}


case class User(
  id: String = UUID.randomUUID.toString,
  loginInfo: LoginInfo,
  socials: Option[Seq[LoginInfo]] = None,
  email: Option[String],
  name: Option[String],
  avatarURL: Option[String],
  roles: Set[Role] = Set(SimpleUser)
) extends Identity {

}

