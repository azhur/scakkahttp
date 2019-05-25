package io.azhur.scalacamp.validator

/**
  * Implement validator typeclass that should validate arbitrary value [T].
  * @tparam T the type of the value to be validated.
  */
trait Validator[T] { me =>
  /**
    * Validates the value.
    * @param value value to be validated.
    * @return Right(value) in case the value is valid, Left(message) on invalid value
    */
  def validate(value: T): Either[String, T]

  /**
    * And combinator.
    * @param other validator to be combined with 'and' with this validator.
    * @return the Right(value) only in case this validator and <code>other</code> validator returns valid value,
    *         otherwise Left with error messages from the validator that failed.
    */
  def and(other: Validator[T]): Validator[T] = new Validator[T] {
    /**
      * Validates the value.
      *
      * @param value value to be validated.
      * @return Right(value) in case the value is valid, Left(message) on invalid value
      */
    override def validate(value: T): Either[String, T] = me.validate(value).flatMap(_ => other.validate(value))
  }

  /**
    * Or combinator.
    * @param other validator to be combined with 'or' with this validator.
    * @return the Right(value) only in case either this validator or <code>other</code> validator returns valid value,
    *         otherwise Left with error messages from both validators.
    */
  def or(other: Validator[T]): Validator[T] = new Validator[T] {
    /**
      * Validates the value.
      *
      * @param value value to be validated.
      * @return Right(value) in case the value is valid, Left(message) on invalid value
      */
    override def validate(value: T): Either[String, T] =
      me.validate(value).left.flatMap(err1 => other.validate(value).left.map(err2 => s"$err1\n$err2"))
  }
}

object Validator {

  val nonEmpty : Validator[String] = new Validator[String] {
    // implement me
    override def validate(t: String): Either[String, String] = Either.cond(t.nonEmpty, t, "value should not be empty")
  }

  val alphanumeric: Validator[String] = new Validator[String] {
    override def validate(value: String): Either[String, String] = Either.cond(value.forall(_.isLetterOrDigit), value, "value should be alphanumeric")
  }
}