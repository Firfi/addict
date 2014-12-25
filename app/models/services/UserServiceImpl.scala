package models.services

import java.util.UUID
import javax.inject.Inject
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.users.User
import play.api.libs.concurrent.Execution.Implicits._
import com.mohiva.play.silhouette.api.LoginInfo
import scala.concurrent.Future
import models.daos.UserDAO

/**
 * Handles actions to users.
 *
 * @param userDAO The user DAO implementation.
 */
class UserServiceImpl @Inject() (userDAO: UserDAO) extends UserService {

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User) = userDAO.save(user)

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  def save(profile: CommonSocialProfile) = {
    userDAO.find(profile.loginInfo).flatMap {
      case Some(user) => // Update user with profile
        userDAO.save(user.copy(
          name = profile.name,
          email = profile.email,
          avatarURL = profile.avatarURL
        ))
      case None => // Insert a new user
        userDAO.save(User(
          loginInfo = profile.loginInfo,
          email = profile.email, // TODO check for email too
          name = profile.name,
          avatarURL = profile.avatarURL
        ))
    }
  }

  implicit class ProfileMethods(profile: CommonSocialProfile) {
    val name = {
      val names = Seq(profile.firstName, profile.lastName, profile.fullName).filter(_.isDefined).map(_.get)
      if (names.nonEmpty) Some(names.mkString(" ")) else None
    }
  }

  override def retrieve(id: String): Future[Option[User]] = userDAO.find(id)
}
