package models.daos

import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.daos.AuthenticatorDAO
import play.libs.F

import scala.concurrent.Future

import JWTAuthenticatorDAO._

class JWTAuthenticatorDAO extends AuthenticatorDAO[JWTAuthenticator] {

  override def save(authenticator: JWTAuthenticator): Future[JWTAuthenticator] = {
    data += (authenticator.id -> authenticator)
    Future.successful(authenticator)
  }

  override def remove(id: String): Future[Unit] = {
    Future.successful(data.remove(id))
  }

  override def find(id: String): Future[Option[JWTAuthenticator]] = {
    Future.successful(data.get(id))
  }
}

object JWTAuthenticatorDAO {
  var data: collection.mutable.HashMap[String, JWTAuthenticator] = collection.mutable.HashMap()
}
