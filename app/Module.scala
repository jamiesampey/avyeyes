import play.api.inject.{Binding, Module => PlayModule}
import play.api.{Configuration, Environment, Logger}

class Module extends PlayModule {
  def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[Logger].toInstance(Logger("avyeyes"))
    )
  }
}
