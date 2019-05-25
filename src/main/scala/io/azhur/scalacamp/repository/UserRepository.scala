package io.azhur.scalacamp.repository

import java.sql.{SQLTimeoutException, SQLTransientException}
import java.util.concurrent.atomic.AtomicLong

import io.azhur.scalacamp.model.{RegisterUserData, User}
import io.azhur.scalacamp.util.Retrier
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds
import scala.concurrent.duration._

trait UserRepository[F[_]] {
  def registerUser(userData: RegisterUserData): F[User]

  def getById(id: Long): F[Option[User]]

  def getByUsername(username: String): F[Option[User]]
}

class SlickUserRepository(val dbConfig: DatabaseConfig[JdbcProfile])
                         (implicit ex: ExecutionContext)
  extends UserRepository[Future] {

  val retriableExceptions: Seq[Class[_ <: Throwable]] = List(classOf[SQLTimeoutException], classOf[SQLTransientException])

  import dbConfig.profile.api._
  private val db = dbConfig.db

  class UserTable(tag: Tag) extends Table[User](tag, "users") {
    val id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    val username = column[String]("username", O.Unique)
    val address = column[Option[String]]("address")
    val email = column[String]("email")

    def * = (id, username, address, email).mapTo[User]
    def insertProjection = (username, address, email).mapTo[RegisterUserData]
  }

  lazy val users = TableQuery[UserTable]
  private lazy val insertQuery = users.map(_.insertProjection) returning users.map(_.id) into ((data, id) => User(id, data.username, data.address, data.email))

  override def registerUser(data: RegisterUserData): Future[User] = {
    val action = insertQuery += data
    runWithRetries(action)
  }

  override def getById(id: Long): Future[Option[User]] = {
    val action = users.filter(_.id === id).result.headOption
    runWithRetries(action)
  }

  override def getByUsername(username: String): Future[Option[User]] = {
    val action = users.filter(_.username === username).result.headOption
    runWithRetries(action)
  }

  private def runWithRetries[T](action: => DBIO[T]): Future[T]  = {
    Retrier.retryFailuresAsync(
      block = () => db.run(action),
      retryExceptions = retriableExceptions,
      retries = List(100.millis, 200.millis, 400.millis)
    )
  }
}

class InMemoryUserRepositoryFuture(implicit val ec: ExecutionContext) extends UserRepository[Future] {
  private val storage = scala.collection.mutable.Map.empty[Long, User]
  private val idGen = new AtomicLong(0L)
  private val retries = List(1.second, 2.seconds, 4.seconds)
  private val retryExceptions = Seq()

  override def registerUser(userData: RegisterUserData): Future[User] = Future {
    val id = idGen.incrementAndGet()
    val user = User(id, userData.username, userData.address, userData.email)
    storage += id -> user
    user
  }

  override def getById(id: Long): Future[Option[User]] = {
    Retrier.retryFailuresAsync(
      block = () => Future {storage.get(id)},
      retryExceptions = retryExceptions,
      retries = retries
    )
  }

  override def getByUsername(username: String): Future[Option[User]] = {
    Retrier.retryFailuresAsync(
      block = () => Future {storage.values.find(_.username == username)},
      retryExceptions = retryExceptions,
      retries = retries
    )
  }
}