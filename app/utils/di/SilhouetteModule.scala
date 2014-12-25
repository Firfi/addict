package utils.di

import com.mohiva.play.silhouette.impl.authenticators.{JWTAuthenticatorSettings, JWTAuthenticatorService, JWTAuthenticator}
import com.mohiva.play.silhouette.impl.daos.{AuthenticatorDAO, DelegableAuthInfoDAO}
import com.mohiva.play.silhouette.impl.providers.oauth1.services.PlayOAuth1Service
import com.mohiva.play.silhouette.impl.providers.oauth2.state.{CookieStateProvider, CookieStateSettings}
import com.mohiva.play.silhouette.impl.services.{GravatarService, DelegableAuthInfoService}
import com.mohiva.play.silhouette.impl.util.PlayCacheLayer
import models.users.User
import play.api.Play
import play.api.Play.current
import play.Logger
import com.google.inject.{ Provides, AbstractModule }
import net.codingwell.scalaguice.ScalaModule
import com.mohiva.play.silhouette.api.{EventBus, Environment}
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.impl.util._
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth2._
import com.mohiva.play.silhouette.impl.providers.oauth1._
import models.services._
import models.daos._
import models.daos.slick._

/**
 * The Guice module which wires all Silhouette dependencies.
 */
class SilhouetteModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  def configure() {
    bind[UserService].to[UserServiceImpl]
    bind[PostService].to[PostServiceImpl]
    bind[BlogService].to[BlogServiceImpl]
    bind[AddictionService].to[AddictionServiceImpl]
//    val useSlick = Play.configuration.getBoolean("silhouette.seed.db.useSlick").getOrElse(false)
//    if (useSlick) {
    Logger.debug("Binding to Slick DAO implementations.")
    bind[UserDAO].to[UserDAOSlick]
    bind[PostDAO].to[PostDAOSlick]
    bind[BlogDAO].to[BlogDAOSlick]
    bind[AddictionDAO].to[AddictionDAOSlick]
    bind[DelegableAuthInfoDAO[PasswordInfo]].to[PasswordInfoDAOSlick]
    bind[DelegableAuthInfoDAO[OAuth1Info]].to[OAuth1InfoDAOSlick]
    bind[DelegableAuthInfoDAO[OAuth2Info]].to[OAuth2InfoDAOSlick]
//    } else {
//      Logger.debug("Binding to In-Memory DAO implementations.")
//      bind[UserDAO].to[UserDAOImpl]
//      bind[DelegableAuthInfoDAO[PasswordInfo]].to[PasswordInfoDAO]
//      bind[DelegableAuthInfoDAO[OAuth1Info]].to[OAuth1InfoDAO]
//      bind[DelegableAuthInfoDAO[OAuth2Info]].to[OAuth2InfoDAO]
//    }
    bind[AuthenticatorDAO[JWTAuthenticator]].to[JWTAuthenticatorDAO]
    bind[CacheLayer].to[PlayCacheLayer]
    bind[HTTPLayer].to[PlayHTTPLayer]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher)
    bind[EventBus].toInstance(EventBus())
  }

  /**
   * Provides the Silhouette environment.
   *
   * @param userService The user service implementation.
   * @param authenticatorService The authentication service implementation.
   * @param eventBus The event bus instance.
   * @return The Silhouette environment.
   */
  @Provides
  def provideEnvironment(
                          userService: UserService,
                          authenticatorService: AuthenticatorService[JWTAuthenticator],
                          eventBus: EventBus,
                          credentialsProvider: CredentialsProvider,
                          facebookProvider: FacebookProvider,
                          googleProvider: GoogleProvider,
                          twitterProvider: TwitterProvider): Environment[User, JWTAuthenticator] = {

    Environment[User, JWTAuthenticator](
      userService,
      authenticatorService,
      Map(
        credentialsProvider.id -> credentialsProvider,
        facebookProvider.id -> facebookProvider,
        googleProvider.id -> googleProvider,
        twitterProvider.id -> twitterProvider
      ),
      eventBus
    )
  }

  /**
   * Provides the authenticator service.
   *
   * @param authenticator The cache layer implementation.
   * @param idGenerator The ID generator used to create the authenticator ID.
   * @return The authenticator service.
   */
  @Provides
  def provideAuthenticatorService(
                                   authenticator: AuthenticatorDAO[JWTAuthenticator],
                                   idGenerator: IDGenerator): AuthenticatorService[JWTAuthenticator] = {
    new JWTAuthenticatorService(JWTAuthenticatorSettings(sharedSecret = "42"), Some(authenticator), idGenerator, Clock())
  }

  /**
   * Provides the auth info service.
   *
   * @param passwordInfoDAO The implementation of the delegable password auth info DAO.
   * @param oauth1InfoDAO The implementation of the delegable OAuth1 auth info DAO.
   * @param oauth2InfoDAO The implementation of the delegable OAuth2 auth info DAO.
   * @return The auth info service instance.
   */
  @Provides
  def provideAuthInfoService(
                              passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo],
                              oauth1InfoDAO: DelegableAuthInfoDAO[OAuth1Info],
                              oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info]): AuthInfoService = {

    new DelegableAuthInfoService(passwordInfoDAO, oauth1InfoDAO, oauth2InfoDAO)
  }

  /**
   * Provides the avatar service.
   *
   * @param httpLayer The HTTP layer implementation.
   * @return The avatar service implementation.
   */
  @Provides
  def provideAvatarService(httpLayer: HTTPLayer): AvatarService = new GravatarService(httpLayer)

  /**
   * Provides the credentials provider.
   *
   * @param authInfoService The auth info service implemenetation.
   * @param passwordHasher The default password hasher implementation.
   * @return The credentials provider.
   */
  @Provides
  def provideCredentialsProvider(
                                  authInfoService: AuthInfoService,
                                  passwordHasher: PasswordHasher): CredentialsProvider = {

    new CredentialsProvider(authInfoService, passwordHasher, Seq(passwordHasher))
  }

  @Provides
  def provideOAuth2StateProvider(idGenerator: IDGenerator): OAuth2StateProvider = {
    new CookieStateProvider(CookieStateSettings(
      cookieName = Play.configuration.getString("silhouette.oauth2StateProvider.cookieName").get,
      cookiePath = Play.configuration.getString("silhouette.oauth2StateProvider.cookiePath").get,
      cookieDomain = Play.configuration.getString("silhouette.oauth2StateProvider.cookieDomain"),
      secureCookie = Play.configuration.getBoolean("silhouette.oauth2StateProvider.secureCookie").get,
      httpOnlyCookie = Play.configuration.getBoolean("silhouette.oauth2StateProvider.httpOnlyCookie").get,
      expirationTime = Play.configuration.getInt("silhouette.oauth2StateProvider.expirationTime").get
    ), idGenerator, Clock())
  }

  /**
   * Provides the Facebook provider.
   *
   * @param httpLayer The HTTP layer implementation.
   * @param stateProvider The OAuth2 state provider implementation.
   * @return The Facebook provider.
   */
  @Provides
  def provideFacebookProvider(httpLayer: HTTPLayer, stateProvider: OAuth2StateProvider): FacebookProvider = {
    FacebookProvider(httpLayer, stateProvider, OAuth2Settings(
      authorizationURL = Play.configuration.getString("silhouette.facebook.authorizationURL").get,
      accessTokenURL = Play.configuration.getString("silhouette.facebook.accessTokenURL").get,
      redirectURL = Play.configuration.getString("silhouette.facebook.redirectURL").get,
      clientID = Play.configuration.getString("silhouette.facebook.clientID").get,
      clientSecret = Play.configuration.getString("silhouette.facebook.clientSecret").get,
      scope = Play.configuration.getString("silhouette.facebook.scope")))
  }

  /**
   * Provides the Google provider.
   *
   * @param httpLayer The HTTP layer implementation.
   * @param stateProvider The OAuth2 state provider implementation.
   * @return The Google provider.
   */
  @Provides
  def provideGoogleProvider(httpLayer: HTTPLayer, stateProvider: OAuth2StateProvider): GoogleProvider = {
    GoogleProvider(httpLayer, stateProvider, OAuth2Settings(
      authorizationURL = Play.configuration.getString("silhouette.google.authorizationURL").get,
      accessTokenURL = Play.configuration.getString("silhouette.google.accessTokenURL").get,
      redirectURL = Play.configuration.getString("silhouette.google.redirectURL").get,
      clientID = Play.configuration.getString("silhouette.google.clientID").get,
      clientSecret = Play.configuration.getString("silhouette.google.clientSecret").get,
      scope = Play.configuration.getString("silhouette.google.scope")))
  }

  /**
   * Provides the Twitter provider.
   *
   * @param cacheLayer The cache layer implementation.
   * @param httpLayer The HTTP layer implementation.
   * @return The Twitter provider.
   */
  @Provides
  def provideTwitterProvider(cacheLayer: CacheLayer, httpLayer: HTTPLayer): TwitterProvider = {
    val settings = OAuth1Settings(
      requestTokenURL = Play.configuration.getString("silhouette.twitter.requestTokenURL").get,
      accessTokenURL = Play.configuration.getString("silhouette.twitter.accessTokenURL").get,
      authorizationURL = Play.configuration.getString("silhouette.twitter.authorizationURL").get,
      callbackURL = Play.configuration.getString("silhouette.twitter.callbackURL").get,
      consumerKey = Play.configuration.getString("silhouette.twitter.consumerKey").get,
      consumerSecret = Play.configuration.getString("silhouette.twitter.consumerSecret").get)

    TwitterProvider(cacheLayer, httpLayer, new PlayOAuth1Service(settings), settings)
  }
}
