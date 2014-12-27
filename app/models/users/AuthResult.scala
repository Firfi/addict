package models.users

import security.models.Token


case class AuthResult(profile: ProfileResult, token: Token)