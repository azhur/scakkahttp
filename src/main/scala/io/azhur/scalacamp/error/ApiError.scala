package io.azhur.scalacamp.error

// error response format returned by http endpoints
case class ApiError(code: Int, status: Int, message: String)

// service level error
sealed trait ServiceError extends Product with Serializable {
  def status: Int
  def code: Int
  def message: String
  def throwable: Option[Throwable]

  def toApiError = ApiError(code, status, message)
}

case class UserNotFound(userId: Long) extends ServiceError {
  override def status: Int = 404
  override def code: Int = 4000
  override def message: String = s"User $userId not found"
  override def throwable: Option[Throwable] = None
}

case class UserAlreadyExists(username: String) extends ServiceError {
  override def status: Int = 409
  override def code: Int = 4001
  override def message: String = s"User $username already exists"
  override def throwable: Option[Throwable] = None
}

case class ValidationError(error: String) extends ServiceError {
  override def status: Int = 400
  override def code: Int = 4002
  override def message: String = s"ValidationError: $error"
  override def throwable: Option[Throwable] = None
}

case class InternalServiceError(err: Throwable) extends ServiceError {
  override def status: Int = 500
  override def code: Int = 4005
  override def message: String = s"InternalServiceError: ${err.getMessage}"
  override def throwable: Option[Throwable] = Some(err)
}