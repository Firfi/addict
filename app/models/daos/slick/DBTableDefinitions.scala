package models.daos.slick

import play.api.db.slick.Config.driver.simple._

import scala.slick.lifted.ProvenShape

object DBTableDefinitions {

  case class DBUser (
    id: String,
    name: Option[String],
    email: Option[String],
    avatarURL: Option[String]
  )

  class Users(tag: Tag) extends Table[DBUser](tag, "user") {
    def id = column[String]("id", O.PrimaryKey)
    def name = column[Option[String]]("name")
    def email = column[Option[String]]("email")
    def avatarURL = column[Option[String]]("avatarURL")

    def * = (id, name, email, avatarURL) <> (DBUser.tupled, DBUser.unapply)
  }

  case class DBLoginInfo (
    id: Option[Long],
    providerID: String,
    providerKey: String
  )

  class LoginInfos(tag: Tag) extends Table[DBLoginInfo](tag, "logininfo") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def providerID = column[String]("providerID")
    def providerKey = column[String]("providerKey")
    def * = (id.?, providerID, providerKey) <> (DBLoginInfo.tupled, DBLoginInfo.unapply)
  }

  case class DBUserLoginInfo (
    userID: String,
    loginInfoId: Long
  )

  class UserLoginInfos(tag: Tag) extends Table[DBUserLoginInfo](tag, "userlogininfo") {
    def userID = column[String]("userID", O.NotNull)
    def loginInfoId = column[Long]("loginInfoId", O.NotNull)
    def * = (userID, loginInfoId) <> (DBUserLoginInfo.tupled, DBUserLoginInfo.unapply)
  }

  case class DBPasswordInfo (
    hasher: String,
    password: String,
    salt: Option[String],
    loginInfoId: Long
  )

  class PasswordInfos(tag: Tag) extends Table[DBPasswordInfo](tag, "passwordinfo") {
    def hasher = column[String]("hasher")
    def password = column[String]("password")
    def salt = column[Option[String]]("salt")
    def loginInfoId = column[Long]("loginInfoId")
    def * = (hasher, password, salt, loginInfoId) <> (DBPasswordInfo.tupled, DBPasswordInfo.unapply)
  }

  case class DBOAuth1Info (
    id: Option[Long],
    token: String,
    secret: String,
    loginInfoId: Long
  )

  class OAuth1Infos(tag: Tag) extends Table[DBOAuth1Info](tag, "oauth1info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def token = column[String]("token")
    def secret = column[String]("secret")
    def loginInfoId = column[Long]("loginInfoId")
    def * = (id.?, token, secret, loginInfoId) <> (DBOAuth1Info.tupled, DBOAuth1Info.unapply)
  }

  case class DBOAuth2Info (
    id: Option[Long],
    accessToken: String,
    tokenType: Option[String],
    expiresIn: Option[Int],
    refreshToken: Option[String],
    loginInfoId: Long
  )

  class OAuth2Infos(tag: Tag) extends Table[DBOAuth2Info](tag, "oauth2info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def accessToken = column[String]("accesstoken")
    def tokenType = column[Option[String]]("tokentype")
    def expiresIn = column[Option[Int]]("expiresin")
    def refreshToken = column[Option[String]]("refreshtoken")
    def loginInfoId = column[Long]("logininfoid")
    def * = (id.?, accessToken, tokenType, expiresIn, refreshToken, loginInfoId) <> (DBOAuth2Info.tupled, DBOAuth2Info.unapply)
  }

  case class DBAddiction (
                      name: String
                         )

  class Addictions(tag: Tag) extends Table[DBAddiction](tag, "addiction") {
    def name = column[String]("name", O.PrimaryKey)

    override def * : ProvenShape[DBAddiction] = name <> (DBAddiction.apply, DBAddiction.unapply)
  }

  case class DBBlog (
                      id: String,
                      userID: String,
                      addictionID: String
                      )

  class Blogs(tag: Tag) extends Table[DBBlog](tag, "blog") {
    def id = column[String]("id", O.PrimaryKey, O.NotNull)
    def userID = column[String]("userid")
    def addictionID = column[String]("addictionid")

    override def * : ProvenShape[DBBlog] = (id, userID, addictionID) <> (DBBlog.tupled, DBBlog.unapply)
  }

  case class DBPost (
                      id: String,
                      userID: String,
                      text: Option[String],
                      measurement: Option[Long],
                      blogID: String,
                      accessLevel: Int
                      )

  class Posts(tag: Tag) extends Table[DBPost](tag, "post") {
    def id = column[String]("id", O.PrimaryKey)
    def userID = column[String]("userid")
    def text = column[Option[String]]("text")
    def measurement = column[Option[Long]]("measurement")
    def blogID = column[String]("blogid")
    def accessLevel = column[Int]("accesslevel")

    override def * : ProvenShape[DBPost] = (id, userID, text, measurement, blogID, accessLevel) <> (DBPost.tupled, DBPost.unapply)
  }

  val slickUsers = TableQuery[Users]
  val slickLoginInfos = TableQuery[LoginInfos]
  val slickUserLoginInfos = TableQuery[UserLoginInfos]
  val slickPasswordInfos = TableQuery[PasswordInfos]
  val slickOAuth1Infos = TableQuery[OAuth1Infos]
  val slickOAuth2Infos = TableQuery[OAuth2Infos]
  val slickPosts = TableQuery[Posts]
  val slickBlogs = TableQuery[Blogs]
  val slickAddictions = TableQuery[Addictions]

}
