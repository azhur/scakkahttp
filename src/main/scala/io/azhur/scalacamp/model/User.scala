package io.azhur.scalacamp.model

case class RegisterUserData(username: String, address: Option[String], email: String)
case class RegisterUserResponse(id: Long)
case class User(id: Long, username: String, address: Option[String], email: String)
