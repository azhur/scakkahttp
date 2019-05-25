package io.azhur.scalacamp.validator

import io.azhur.scalacamp.model.RegisterUserData

object UserValidator {
  implicit val registerUserDataValidator = new Validator[RegisterUserData] {
    import Validator._
    override def validate(value: RegisterUserData): Either[String, RegisterUserData] = {
      val validatedUsername = (nonEmpty and alphanumeric) validate value.username
      validatedUsername.map(_ => value)
    }
  }
}
