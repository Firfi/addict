package security.formatters.json

import com.mohiva.play.silhouette.impl.providers.OAuth1Info
import play.api.libs.json._
import play.api.libs.functional.syntax._

object OAuth1InfoFormats {
  
  implicit val restFormat = (
    (__ \ "token").format[String] ~
    (__ \ "secret").format[String])(OAuth1Info.apply, unlift(OAuth1Info.unapply))

}