package formatters.json

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.posts.Post
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.authorizations._
import models.users._

/**
 * This object contains all format for User class
 */
object UserFormats {

  val restFormat = {

    val reader = (
      (__ \ "id").read[String] ~
        (__ \ "loginInfo").read[LoginInfo] ~
        (__ \ "socials").readNullable(Reads.seq[LoginInfo]) ~
        (__ \ "email").readNullable(Reads.email) ~
        (__ \ "username").readNullable[String] ~
        (__ \ "avatarUrl").readNullable[String] ~
      (__ \ "roles").readNullable(Reads.set[String]).map { case Some(r) => r.map(Role.apply) case None => Set[Role](SimpleUser) })(User.apply _)

    val writer = (
      (__ \ "id").write[String] ~
      (__ \ "loginInfo").write[LoginInfo] ~
      (__ \ "socials").writeNullable(Writes.seq[LoginInfo]) ~
      (__ \ "email").writeNullable[String] ~
      (__ \ "name").writeNullable[String] ~
      (__ \ "avatarUrl").writeNullable[String] ~
      (__ \ "roles").write(Writes.set[String]).contramap[Set[Role]](_.map(_.name)))(unlift(User.unapply _))

    Format(reader, writer)
  }

}