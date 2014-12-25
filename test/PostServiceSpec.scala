import _root_.Utils.{AddictSlickModule, WithInjectedApplication}
import com.google.inject.{Guice, Inject}
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models.daos.{AddictionDAO, UserDAO, BlogDAO, PostDAO}
import models.daos.slick.{AddictionDAOSlick, UserDAOSlick, BlogDAOSlick, PostDAOSlick}
import models.posts.{BlogRequest, PostRequest}
import models.services._
import models.users.User
import net.codingwell.scalaguice.ScalaModule
import org.junit.runner.RunWith
import org.specs2.execute.{Result, AsResult}
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.Helpers._
import play.api.test.{PlaySpecification, FakeRequest, WithApplication}
import utils.di.SilhouetteModule
import com.mohiva.play.silhouette.test._



@RunWith(classOf[JUnitRunner])
class PostServiceSpec extends PlaySpecification {



  "Post Service" should {
    "create post" in new context {
      await {
        postService.save(PostRequest(blog = blog.id), fakeIdentity)
      } should beRight
    }
    "fetch post" in new postContext {
      await {
        postService.fetch(post.id)
      } should beSome
    }
  }

  class PostModule extends AddictSlickModule {
    override def configure(): Unit = {
      super.configure()
      bind[UserService].to[UserServiceImpl]
      bind[UserDAO].to[UserDAOSlick]
      bind[PostService].to[PostServiceImpl]
      bind[PostDAO].to[PostDAOSlick]
      bind[BlogService].to[BlogServiceImpl]
      bind[BlogDAO].to[BlogDAOSlick]
    }
  }

  trait context extends WithInjectedApplication {
    def module = new PostModule
    @Inject var postService: PostService = null
    @Inject var blogService: BlogService = null
    @Inject var userService: UserService = null

    val blog = BlogRequest(addictionId = "Alcohol")

    override def around[T: AsResult](t: => T): Result = super.around {
      userService.save(fakeIdentity)
      blogService.save(blog, fakeIdentity)
      t
    }
  }

  trait postContext extends context {

    val post = PostRequest(blog = blog.id)

    override def around[T: AsResult](t: => T): Result = super.around {
      postService.save(post, fakeIdentity)
      t
    }
  }

}
