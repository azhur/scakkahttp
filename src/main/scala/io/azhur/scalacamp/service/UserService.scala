package io.azhur.scalacamp.service

import cats.Monad
import io.azhur.scalacamp.model.{RegisterUserData, User}
import io.azhur.scalacamp.repository.UserRepository

import scala.language.higherKinds
import cats.implicits._
import io.azhur.scalacamp.error.{ServiceError, UserAlreadyExists, UserNotFound}

class UserService[F[_]](repository: UserRepository[F])
                       (implicit monad: Monad[F]) {
  def registerUser(userData: RegisterUserData): F[Either[ServiceError, User]] = {
    // .flatMap syntax works because of import cats.implicits._
    // so flatMap function is added to F[_] through implicit conversions
    // The implicit monad param knows how to flatmap and map over your F.
    repository.getByUsername(userData.username).flatMap({
      case Some(user) =>
        monad.pure(Left(UserAlreadyExists(user.username)))
      case None =>
        // .map syntax works because of import cats.implicits._
        // so map function is added to F[_] through implicit conversions
        repository.registerUser(userData).map(Right(_))
    })
  }

  def getById(id: Long): F[Either[ServiceError, User]] =
    repository.getById(id).map(mu => Either.fromOption(mu, UserNotFound(id)))
}
