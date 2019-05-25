package io.azhur.scalacamp

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import io.azhur.scalacamp.model.RegisterUserData
import io.azhur.scalacamp.repository.SlickUserRepository
import io.azhur.scalacamp.service.UserService
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import cats.implicits._

class UserRoutesSpec
  extends WordSpec
    with Matchers
    with ScalaFutures
    with JsonSupport
    with ScalatestRouteTest {

  private val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("database")
  private val repository = new SlickUserRepository(dbConfig)
  private val userService = new UserService(repository)
  private val routes = new UserRoutes(userService).routes

  "UserRoutes" should {
    "register user (POST /user)" in {
      val user = RegisterUserData("John", None, email = "test@test.co")
      val userEntity = Marshal(user).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      // using the RequestBuilding DSL:
      Post("/user").withEntity(userEntity) ~> routes ~> check {
        status should ===(StatusCodes.Created)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and we know what message we're expecting back:
        entityAs[String] should ===("""{"id":1}""")
      }
    }

    "return 409 on duplicate user (POST /user)" in {
      val user = RegisterUserData("admin", None, email = "test@test.co")
      val userEntity = Marshal(user).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      repository.registerUser(user).futureValue

      // using the RequestBuilding DSL:
      Post("/user").withEntity(userEntity) ~> routes ~> check {
        status should ===(StatusCodes.Conflict)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and we know what message we're expecting back:
        entityAs[String] should ===("""{"code":4001,"message":"User admin already exists","status":409}""")
      }
    }

    "return 400 on invalid user data (POST /user)" in {
      val user = RegisterUserData("@dmin", None, email = "test@test.co")
      val userEntity = Marshal(user).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      // using the RequestBuilding DSL:
      Post("/user").withEntity(userEntity) ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and we know what message we're expecting back:
        entityAs[String] should ===("""{"code":4002,"message":"ValidationError: value should be alphanumeric","status":400}""")
      }
    }

    "return the present user on (GET /user)" in {
      val user = RegisterUserData("adminische", Some("address"), email = "admin@mail.com")
      val newUser = repository.registerUser(user).futureValue

      Get(s"/user?id=${newUser.id}") ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===(s"""{"address":"address","email":"admin@mail.com","id":${newUser.id},"username":"adminische"}""")
      }
    }

    "return 404 on not found user (GET /user)" in {
      Get("/user?id=1000") ~> routes ~> check {
        status should ===(StatusCodes.NotFound)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===("""{"code":4000,"message":"User 1000 not found","status":404}""")
      }
    }

    "return 400 on MissingQueryParamRejection (GET /user)" in {
      Get("/user") ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===("""{"code":1004,"message":"Missing required query parameter: id","status":400}""")
      }
    }
  }
}