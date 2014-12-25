package security.formatters.json
import play.api.libs.json.Reads._
import com.mohiva.play.silhouette.api.util.Credentials
import play.api.libs.json._
import play.api.libs.functional.syntax._
import security.models.SignUp

object CredentialFormats {

  val passwordFormat = minLength[String](8)

  implicit val credentialsReads = ((__ \ "identifier").read[String](email) ~
    (__ \ "password").read[String](passwordFormat))(Credentials.apply _)

  implicit val signUpCredentials = ((__ \ "identifier").read[String](email) ~
    (__ \ "password").read[String](passwordFormat) ~ (__ \ "name").read[Option[String]])(SignUp.apply _)

}