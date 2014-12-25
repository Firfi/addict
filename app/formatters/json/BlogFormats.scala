package formatters.json

import models.posts._
import play.api.libs.json._

trait BlogFormats extends AddictionFormats {
  implicit val blogFormats = Json.format[Blog]
  implicit val requestBlogFormats = Json.format[BlogRequest]
}
