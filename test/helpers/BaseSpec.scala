package helpers

import org.specs2.mock.Mockito
import play.api.test.PlaySpecification

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

trait BaseSpec extends PlaySpecification with Mockito with Generators {

  implicit class FutureOps[T](future: Future[T]) {
    def resolve: T = Await.result(future, Duration.Inf)
  }
}
