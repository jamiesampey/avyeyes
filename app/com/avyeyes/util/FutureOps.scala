package com.avyeyes.util

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object FutureOps {
  implicit class FutureOps[T](future: Future[T]) {
    def resolve: T = Await.result(future, Duration.Inf)
  }
}
