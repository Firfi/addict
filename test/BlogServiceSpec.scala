import Utils.{AddictSlickModule, WithInjectedApplication}
import com.google.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import models.daos.{BlogDAO, UserDAO, AddictionDAO}
import models.daos.slick.{BlogDAOSlick, UserDAOSlick, AddictionDAOSlick}
import models.posts.BlogRequest
import models.services._
import models.users.User
import net.codingwell.scalaguice.ScalaModule
import org.specs2.execute.{Result, AsResult}
import play.api.test.PlaySpecification

class BlogServiceSpec extends PlaySpecification {

  "blog" should {
    "be saved" in new context {
      await {
        blogService.save(blog, fakeIdentity)
      } should beRight
    }
  }


  class BlogModule extends AddictSlickModule {
    override def configure(): Unit = {
      super.configure()
      bind[BlogService].to[BlogServiceImpl]
      bind[BlogDAO].to[BlogDAOSlick]
      bind[UserService].to[UserServiceImpl]
      bind[UserDAO].to[UserDAOSlick]
    }
  }

  trait context extends WithInjectedApplication {

    val blog = BlogRequest(addictionId = "Alcohol")

    def module = new BlogModule
    @Inject var blogService: BlogService = null
    @Inject var userService: UserService = null

    override def around[T: AsResult](t: => T): Result = super.around {
      userService.save(fakeIdentity)
      t
    }

  }


}
