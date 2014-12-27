package formatters.json

import models.posts.Addiction
import play.api.libs.json.Json

trait AddictionFormats {
  implicit val addictionFormats = Json.format[Addiction]
}

object AddictionFormats extends AddictionFormats
