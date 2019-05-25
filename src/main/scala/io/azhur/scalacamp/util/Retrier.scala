package io.azhur.scalacamp.util

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

object Retrier {
  def retryFailuresAsync[A](block: () => Future[A],
                            retryExceptions: Seq[Class[_ <: Throwable]],
                            retries: List[FiniteDuration])(implicit ec: ExecutionContext): Future[A] = {
    block().recoverWith({
      case e if retryExceptions.exists(_.isAssignableFrom(e.getClass)) && retries.nonEmpty =>
        Thread.sleep(retries.head.toMillis)
        retryFailuresAsync(block, retryExceptions, retries.tail)
    })
  }
}
