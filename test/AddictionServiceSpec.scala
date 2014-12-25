import _root_.Utils.{AddictSlickModule, WithInjectedApplication}
import com.google.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import models.daos.{UserDAO, BlogDAO, AddictionDAO}
import models.daos.slick.{UserDAOSlick, BlogDAOSlick, AddictionDAOSlick}
import models.posts.BlogRequest
import models.services._
import models.users.User
import net.codingwell.scalaguice.ScalaModule
import org.specs2.execute.{Result, AsResult}
import org.specs2.mutable.Specification
import play.api.test.PlaySpecification


class AddictionServiceSpec extends PlaySpecification {
  "addiction service" should {
    "fetch preinitialized addiction" in new context {
      await {
        addictionService.fetch("Alcohol")
      } should beSome
    }
  }

  class AddictionModule extends AddictSlickModule {
  }

  trait context extends WithInjectedApplication {

    def module = new AddictionModule
    @Inject var addictionService: AddictionService = null

  }


}
