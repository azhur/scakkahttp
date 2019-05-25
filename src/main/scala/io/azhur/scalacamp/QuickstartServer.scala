package io.azhur.scalacamp

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import io.azhur.scalacamp.config.ServiceConfig
import io.azhur.scalacamp.repository.InMemoryUserRepositoryFuture
import io.azhur.scalacamp.service.UserService
import cats.implicits._

object QuickstartServer extends App {

  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")

  ServiceConfig.parseConfig() match {
    case Right(config) =>
      //#server-bootstrapping
      startHttpServer(config)

    case Left(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)
  //#http-server
  //#main-class

  private def startHttpServer(config: ServiceConfig)(implicit system: ActorSystem) = {
    // set up ActorSystem and other dependencies here
    //#main-class
    //#server-bootstrapping

    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContext = system.dispatcher

    val userService = new UserService[Future](new InMemoryUserRepositoryFuture())

    val userRoutes = new UserRoutes(userService).routes

    //#http-server
    val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(userRoutes, config.server.host, config.server.port)

    serverBinding.onComplete {
      case Success(_) =>
        println(s"Server online at http://${config.server.host}:${config.server.port}/")
      case Failure(e) =>
        Console.err.println(s"Server could not start!")
        e.printStackTrace()
        system.terminate()
    }
  }
}
