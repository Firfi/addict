package security.formatters.json

import com.mohiva.play.silhouette.api.util.PasswordInfo
import play.api.libs.json._
import play.api.libs.functional.syntax._

object PasswordInfoFormats {

  implicit val restFormat = (
    (__ \ "hasher").format[String] ~
    (__ \ "password").format[String] ~
    (__ \ "salt").formatNullable[String])(PasswordInfo.apply, unlift(PasswordInfo.unapply))

}