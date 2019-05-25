package io.azhur.scalacamp.config

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Try

case class ServerConfig(host: String, port: Int)
case class ServiceConfig(server: ServerConfig, database: Config)

object ServiceConfig {
  def parseConfig(): Either[Throwable, ServiceConfig] = {
    val root = ConfigFactory.load()

    for {
      server   <- parseServerConfig(root)
      database <- parseDatabaseConfig(root)
    } yield ServiceConfig(server, database)
  }

  def parseServerConfig(root: Config): Either[Throwable, ServerConfig] = {
    for {
      sererConfig <- Try(root.getConfig("server")).toEither
      host        <- Try(sererConfig.getString("host")).toEither
      port        <- Try(sererConfig.getInt("port")).toEither
    } yield ServerConfig(host, port)
  }

  def parseDatabaseConfig(root: Config): Either[Throwable, Config] = {
    for {
      databaseConfig <- Try(root.getConfig("database")).toEither
    } yield databaseConfig
  }
}