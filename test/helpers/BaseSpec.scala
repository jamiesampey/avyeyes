package helpers

import java.io.File

import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationLike
import play.api.{Environment, Mode}
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

trait BaseSpec extends SpecificationLike with Mockito with Generators {

  protected val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder()
    .in(Environment.simple(new File("app"), Mode.Test))

  implicit class FutureOps[T](future: Future[T]) {
    def resolve: T = Await.result(future, Duration.Inf)
  }
}
