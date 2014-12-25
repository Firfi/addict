import com.google.inject.{Guice, Module}
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.test.FakeEnvironment
import models.daos.AddictionDAO
import models.daos.slick.AddictionDAOSlick
import models.services.{AddictionServiceImpl, AddictionService, UserServiceImpl, UserService}
import models.users.User
import net.codingwell.scalaguice.ScalaModule
import org.specs2.execute.{Result, AsResult}
import play.api.Logger
import play.api.test.Helpers._
import play.api.test.{PlaySpecification, FakeApplication, WithApplication}
import org.specs2.specification.{Fragments, Step}
import scala.concurrent.duration._

import scala.concurrent.Await

object Utils {
  trait WithInjectedApplication extends WithApplication with SilhouetteTesting {

    def module: Module
    override def around[T: AsResult](t: => T): Result = super.around {
      val injector = Guice.createInjector(module)
      injector.injectMembers(this)
      Await.result({
        Global.initDbValues(injector)
      }, 5000.millis)
      t
    }
  }
  class AddictSlickModule extends ScalaModule {
    override def configure(): Unit = {
      bind[AddictionService].to[AddictionServiceImpl]
      bind[AddictionDAO].to[AddictionDAOSlick]
    }
  }
  object Helpers {
    def fapp = FakeApplication(additionalConfiguration = inMemoryDatabase())
  }

  trait SilhouetteTesting {
    val fakeIdentity = User(loginInfo = LoginInfo("facebook", "apollonia.vanova@watchmen.com"),
      name = Some("Pepka Jumphigh"),
      email = Some("pepka@jumphigh.com"),
      avatarURL = None)

    implicit val fakeEnv = FakeEnvironment[User, JWTAuthenticator](fakeIdentity)
  }

}
