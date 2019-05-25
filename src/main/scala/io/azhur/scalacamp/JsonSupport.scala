package io.azhur.scalacamp

import io.azhur.scalacamp.error.ApiError
import io.azhur.scalacamp.model.{RegisterUserData, RegisterUserResponse, User}

//#json-support
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val registerUserDataFormat = jsonFormat3(RegisterUserData)
  implicit val registerUserResponseFormat = jsonFormat1(RegisterUserResponse)
  implicit val userJsonFormat = jsonFormat4(User)
  implicit val apiErrorFormat = jsonFormat3(ApiError)
}
//#json-support
