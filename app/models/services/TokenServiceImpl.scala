package models.services

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.RequestHeader
import security.models.Token

/**
 * Created by firfi on 12/11/14.
 */
object TokenServiceImpl {
  def tokenFromResponse(requestHeader: RequestHeader) = {
    // temporary TODO http://stackoverflow.com/questions/27417061/reaching-jwt-token-in-play2-silhouette-jwtauthenticator
    val tok = requestHeader.headers.get("X-Auth-Token").get
    Logger.warn(tok)
    tok
  }
}
