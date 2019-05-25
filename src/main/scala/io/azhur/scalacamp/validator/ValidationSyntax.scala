package io.azhur.scalacamp.validator

import scala.language.implicitConversions

trait ValidationSyntax {
  implicit final def anyValidatorSyntax[A](a: A): AnyValidator[A] = new AnyValidator[A](a)
}

object ValidationSyntax extends ValidationSyntax


final class AnyValidator[T](val t: T) extends AnyVal {
  def validate(implicit validator: Validator[T]): Either[String, T] = validator.validate(t)
}
