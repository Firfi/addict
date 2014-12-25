package formatters.json

import com.mohiva.play.silhouette.api.LoginInfo
import formatters.json.JsonExtensions._
import models.authorizations.{SimpleUser, Role}
import models.posts.{PostRequest, Post}
import models.users.User
import play.api.libs.functional.syntax._
import play.api.libs.json._

object PostFormats {
  val restFormat = new Format[Post] {

    import JsonExtensions._

    val base = Json.format[Post]
    def reads(json: JsValue): JsResult[Post] = base.compose(withDefault("id", Post.genId)).reads(json)
    def writes(o: Post): JsValue = base.writes(o)

  }

  val requestRestFormat = new Format[PostRequest] {
    val base = Json.format[PostRequest]
    def reads(json: JsValue): JsResult[PostRequest] = base.compose(withDefault("id", Post.genId)).reads(json)
    def writes(o: PostRequest): JsValue = base.writes(o)
  }

}
