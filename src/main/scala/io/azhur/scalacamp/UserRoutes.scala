package io.azhur.scalacamp

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import cats.data.EitherT
import io.azhur.scalacamp.model.{RegisterUserData, RegisterUserResponse}
import io.azhur.scalacamp.service.UserService
import cats.implicits._
import io.azhur.scalacamp.directive.ServiceDirectives
import io.azhur.scalacamp.error.{InternalServiceError, ValidationError}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds
import scala.util.{Failure, Success}

// how to make it tagless final
// http://miklos-martin.github.io/tips/2018/05/25/tagless-final-vs-akka-http-routes.html
class UserRoutes(userService: UserService[Future])(implicit val system: ActorSystem, val ec: ExecutionContext) extends JsonSupport {
  //#user-routes-class
  lazy val log = Logging(system, classOf[UserRoutes])

  // exception handling
  val serviceDirectives = new ServiceDirectives()
  import serviceDirectives.exceptionHandler
  import serviceDirectives.rejectionHandler

  import io.azhur.scalacamp.validator.ValidationSyntax._
  import io.azhur.scalacamp.validator.UserValidator._

  val routes: Route = Route.seal(pathPrefix("user") {
    get {
      parameter('id.as[Long]) { id =>
        onComplete(userService.getById(id)) {
          case Success(Right(value)) =>
            complete(StatusCodes.OK, value)
          case Success(Left(value)) =>
            complete(value.status, value.toApiError)
          case Failure(e) =>
            val value = InternalServiceError(e)
            complete(value.status, value.toApiError)
        }
      }
    } ~ post {
      entity(as[RegisterUserData]) { data =>
        val response = for {
          validData <- EitherT(Future.successful(data.validate.left.map(err => ValidationError(err))))
          user      <- EitherT(userService.registerUser(validData))
        } yield RegisterUserResponse(user.id)

        onComplete(response.value) {
          case Success(Right(value)) =>
            complete(StatusCodes.Created, value)
          case Success(Left(value)) =>
            complete(value.status, value.toApiError)
          case Failure(e) =>
            val value = InternalServiceError(e)
            complete(value.status, value.toApiError)
        }
      }
    }
  })
}
