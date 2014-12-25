import Utils.{AddictSlickModule, WithInjectedApplication}
import akka.util.Timeout
import com.google.inject.{Inject}
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.daos.{AddictionDAO, UserDAO}
import models.services.{AddictionServiceImpl, AddictionService, UserServiceImpl, UserService}
import models.users.User
import net.codingwell.scalaguice.ScalaModule
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.test.{PlaySpecification}
import scala.concurrent.duration._
import models.daos.slick.{AddictionDAOSlick, UserDAOSlick}

@RunWith(classOf[JUnitRunner])
class UserServiceSpec extends PlaySpecification {

  val SIGNUPEMAIL = "signup@test.test"
  val SIGNINEMAIL = "signin@test.test"
  override implicit def defaultAwaitTimeout: Timeout = 0.seconds // literally, it's not endless!

  "this is an example" in new context {
    println("RUNNING TEST")
    userService must not beNull
  }

  "user" should {
    "be created through credentials and then fetched" in new context {
      val saveUser =
        User(name=Some("Test"), avatarURL=None, loginInfo = LoginInfo(CredentialsProvider.ID, SIGNUPEMAIL), email = Some(SIGNUPEMAIL))
      userService.save(saveUser).flatMap { user =>
        userService.retrieve(user.id)
      } should beEqualTo(Some(saveUser)).await
    }
  }

  class UserModule extends AddictSlickModule {
    override def configure(): Unit = {
      super.configure()
      bind[UserService].to[UserServiceImpl]
      bind[UserDAO].to[UserDAOSlick]
    }
  }

  trait context extends WithInjectedApplication {
    def module = new UserModule
    @Inject var userService: UserService = null
  }

}



