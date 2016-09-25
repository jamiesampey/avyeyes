package com.avyeyes

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.Await

package object rest {
  implicit class FutureOps[T](future: Future[T]) {
    def resolve: T = Await.result(future, Duration.Inf)
  }
}
