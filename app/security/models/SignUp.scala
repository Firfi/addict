package security.models

case class SignUp(
  identifier: String,
  password: String,
  name: Option[String]
)