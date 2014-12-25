package models.posts

import java.util.UUID

import models.users.User

object Blog {
  def genId = UUID.randomUUID.toString
}

case class Blog (
                    id: String = Blog.genId,
                    addiction: Addiction
                    )

case class BlogRequest(id: String = Blog.genId,
                       addictionId: String)
