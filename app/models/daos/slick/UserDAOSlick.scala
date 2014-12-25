package models.daos.slick

import com.mohiva.play.silhouette.api.LoginInfo
import models.users.User
import play.api.Logger
import play.api.db.slick._
import play.api.db.slick.Config.driver.simple._
import models.daos.slick.DBTableDefinitions._
import scala.concurrent.Future
import java.util.UUID
import models.daos.UserDAO

/**
 * Give access to the user object using Slick
 */
class UserDAOSlick extends UserDAO {

  import play.api.Play.current

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo) = {
    DB withSession { implicit session =>
      Future.successful {
        slickLoginInfos.filter(
          x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey
        ).firstOption match {
          case Some(info) =>
            slickUserLoginInfos.filter(_.loginInfoId === info.id).firstOption match {
              case Some(userLoginInfo) =>
                slickUsers.filter(_.id === userLoginInfo.userID).firstOption match {
                  case Some(user) =>
                    Some(User(id = user.id,
                      loginInfo = loginInfo,
                      name = user.name,
                      email = user.email,
                      avatarURL = user.avatarURL))
                  case None => None
                }
              case None => None
            }
          case None => None
        }
      }
    }
  }

  /**
   * Finds a user by its user ID.
   *
   * @param id The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(id: String) = {
    DB withSession { implicit session =>
      Future.successful {
        slickUsers.filter(
          _.id === id
        ).firstOption match {
          case Some(user) =>
            slickUserLoginInfos.filter(_.userID === user.id).firstOption match {
              case Some(info) =>
                slickLoginInfos.filter(_.id === info.loginInfoId).firstOption match {
                  case Some(loginInfo) =>
                    Some(User(id = user.id,
                      loginInfo = LoginInfo(loginInfo.providerID, loginInfo.providerKey),
                      name = user.name,
                      email = user.email,
                      avatarURL = user.avatarURL))
                  case None => None
                }
              case None => None
            }
          case None => None
        }
      }
    }
  }

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User) = {
    DB withSession { implicit session =>
      Future.successful {
        val dbUser = DBUser(user.id, user.name, user.email, user.avatarURL)
        slickUsers.filter(_.id === dbUser.id).firstOption match {
          case Some(userFound) => slickUsers.filter(_.id === dbUser.id).update(dbUser)
          case None => slickUsers.insert(dbUser)
        }
        var dbLoginInfo = DBLoginInfo(None, user.loginInfo.providerID, user.loginInfo.providerKey)
        // Insert if it does not exist yet
        slickLoginInfos.filter(info => info.providerID === dbLoginInfo.providerID && info.providerKey === dbLoginInfo.providerKey).firstOption match {
          case None => slickLoginInfos.insert(dbLoginInfo)
          case Some(info) => Logger.debug("Nothing to insert since info already exists: " + info)
        }
        dbLoginInfo = slickLoginInfos.filter(info => info.providerID === dbLoginInfo.providerID && info.providerKey === dbLoginInfo.providerKey).first
        // Now make sure they are connected
        slickUserLoginInfos.filter(info => info.userID === dbUser.id && info.loginInfoId === dbLoginInfo.id).firstOption match {
          case Some(info) =>
            // They are connected already, we could as well omit this case ;)
          case None =>
            slickUserLoginInfos += DBUserLoginInfo(dbUser.id, dbLoginInfo.id.get)
        }
        user // We do not change the user => return it
      }
    }
  }
}
