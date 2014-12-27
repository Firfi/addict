package formatters.json

import com.mohiva.play.silhouette.api.LoginInfo
import models.authorizations.{SimpleUser, Role}
import models.users.{ProfileResult, User}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import security.models.Token

trait ProfileResultFormats extends UserFormats {
  implicit val profileResultFormats = Json.format[ProfileResult]
}

object ProfileResultFormats extends ProfileResultFormats {
}
