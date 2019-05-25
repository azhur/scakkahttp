package io.azhur.scalacamp.directive

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ExceptionHandler, MalformedQueryParamRejection, MalformedRequestContentRejection, MethodRejection, MissingQueryParamRejection, Rejection, RejectionHandler, UnsupportedRequestContentTypeRejection}
import akka.http.scaladsl.server.Directives.{complete, extractUnmatchedPath}
import io.azhur.scalacamp.{JsonSupport, UserRoutes}
import io.azhur.scalacamp.error.ApiError

class ServiceDirectives(implicit system: ActorSystem) extends JsonSupport {
  lazy val log = Logging(system, classOf[UserRoutes])

  implicit val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case t: Throwable =>
      log.warning(s"Exception reached the top level of the API", t)
      complete(StatusCodes.InternalServerError,
        ApiError(3000, StatusCodes.InternalServerError.intValue, s"Exception reached top level of API ${t.getClass.getName} ${t.getMessage}${t.getStackTrace.mkString("\n", "\n", "\n")}")
      )
  }

  implicit val rejectionHandler: RejectionHandler = RejectionHandler
    .newBuilder()
    .handleAll[UnsupportedRequestContentTypeRejection] { _ =>
    complete(
      StatusCodes.BadRequest,
      ApiError(1001, StatusCodes.BadRequest.intValue, "Unsupported Content Type")
    )
  }
    .handleAll[MethodRejection] { _ =>
    complete(
      StatusCodes.MethodNotAllowed,
      ApiError(1002, StatusCodes.MethodNotAllowed.intValue, s"HTTP Method not allowed")
    )
  }
    .handle { case MalformedQueryParamRejection(_, errorMsg, _) =>
      complete(
        StatusCodes.BadRequest,
        ApiError(1003, StatusCodes.BadRequest.intValue, errorMsg)
      )
    }
    .handle { case MissingQueryParamRejection(parameterName) =>
      complete(
        StatusCodes.BadRequest,
        ApiError(1004, StatusCodes.BadRequest.intValue, s"Missing required query parameter: $parameterName")
      )
    }
    .handle { case MalformedRequestContentRejection(msg, _) =>
      complete(
        StatusCodes.BadRequest,
        ApiError(1005, StatusCodes.BadRequest.intValue, msg)
      )
    }
    .handleNotFound {
      extractUnmatchedPath { path =>
        complete(
          StatusCodes.NotFound,
          ApiError(1006, StatusCodes.NotFound.intValue, s"API does not exist: $path")
        )
      }
    }
    .handle { case other: Rejection =>
      log.warning(s"Unhandled rejection, sending http 500 for ${other.getClass.getName} $other")
      complete(
        StatusCodes.InternalServerError,
        ApiError(1007, StatusCodes.InternalServerError.intValue, s"Unrecognized rejection: ${other.getClass.getName} $other")
      )
    }
    .result()
}
