import com.mohiva.play.silhouette.api.exceptions.AuthenticationException
import models.services.AddictionService
import play.api.i18n.{Messages, Lang}
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.GlobalSettings
import play.api.mvc.{Result, RequestHeader}
import com.mohiva.play.silhouette.api.SecuredSettings
import utils.di.SilhouetteModule
import utils.responses.rest.{Bad, Good}
import scala.concurrent.Future
import com.google.inject.{Inject, Guice, Injector}
import controllers.routes
import play.api.Logger

/**
 * The global configuration.
 */
object Global extends GlobalSettings with SecuredSettings {

  /**
   * The Guice dependencies injector.
   */
  var injector: Injector = _

  override def onStart(app: play.api.Application) = {
    super.onStart(app)
    // Now the configuration is read and we can create our Injector.
    val module = new SilhouetteModule()
    injector = Guice.createInjector(module)
    initDbValues(injector)
  }

  def initDbValues(injector: Injector): Future[Any] = {
    val injectee = new {
      @Inject var addictionService: AddictionService = null
    }
    injector.injectMembers(injectee)
    injectee.addictionService.init()
  }


  override def getControllerInstance[A](controllerClass: Class[A]) = injector.getInstance(controllerClass)

  /**
   * Called when a user is not authenticated.
   *
   * As defined by RFC 2616, the status code of the response should be 401 Unauthorized.
   *
   * @param request The request header.
   * @param lang The currently selected language.
   * @return The result to send to the client.
   */
  override def onNotAuthenticated(request: RequestHeader, lang: Lang): Option[Future[Result]] = {
    Some(Future.successful(Unauthorized(Json.toJson(Bad(message = "you are not logged in")))))
  }

  /**
   * Called when a user is authenticated but not authorized.
   *
   * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
   *
   * @param request The request header.
   * @param lang The currently selected language.
   * @return The result to send to the client.
   */
  override def onNotAuthorized(request: RequestHeader, lang: Lang): Option[Future[Result]] = {
    Some(Future.successful(Unauthorized(Json.toJson(Bad(message = "unauthorized")))))
  }

  override def onBadRequest(request: RequestHeader, error: String): Future[Result] = {
    Future.successful(BadRequest(Json.toJson(Bad(message = error))))
  }

}
