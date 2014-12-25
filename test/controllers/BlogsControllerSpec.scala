import Utils.{SilhouetteTesting, AddictSlickModule, WithInjectedApplication}
import com.google.inject.Inject
import controllers.BlogsController
import models.daos.AddictionDAO
import models.daos.slick.AddictionDAOSlick
import models.services.{BlogService, UserService, AddictionServiceImpl, AddictionService}
import net.codingwell.scalaguice.ScalaModule
import org.specs2.mock.Mockito
import play.api.test.{FakeRequest, WithApplication, PlaySpecification}

import scala.concurrent.Future


class BlogsControllerSpec extends PlaySpecification with Mockito with SilhouetteTesting {



  "blogs controller" should {
    "userBlogs" should {
      "return bad status if there's no such user" in new WithApplication with WithMockController {
        val noUser = "noUser"
        mockUserService.retrieve(noUser) returns Future.successful(None)
        val r = blogsController.userBlogs(noUser)(FakeRequest())
        status(r) must_== BAD_REQUEST
        contentAsString(r) must contain("no user")
      }
    }
  }

  trait WithMockController {

    val mockBlogService = mock[BlogService]

    val mockUserService = mock[UserService]

    val blogsController = new BlogsController(mockBlogService,
      mockUserService,
      fakeEnv)
  }

}

