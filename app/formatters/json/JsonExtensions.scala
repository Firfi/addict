package formatters.json

import play.api.libs.json._
import play.api.libs.functional.syntax._

// http://stackoverflow.com/questions/20616677/defaults-for-missing-properties-in-play-2-json-formats

object JsonExtensions {
  def withDefault[A](key: String, default: A)(implicit writes: Writes[A]) =
    __.json.update((__ \ key).json.copyFrom((__ \ key).json.pick orElse Reads.pure(Json.toJson(default))))
}
