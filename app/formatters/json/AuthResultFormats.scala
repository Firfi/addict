package formatters.json

import models.users.AuthResult
import play.api.libs.json.Json

trait AuthResultFormats extends ProfileResultFormats {
  implicit val format = Json.format[AuthResult]
}

object AuthResultFormats extends AuthResultFormats
