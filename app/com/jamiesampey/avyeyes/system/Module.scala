package com.jamiesampey.avyeyes.system

import com.jamiesampey.avyeyes.data.DataMaintenance
import com.google.inject.AbstractModule
import play.api.Logger
import play.libs.akka.AkkaGuiceSupport
import securesocial.core.RuntimeEnvironment

class Module extends AbstractModule with AkkaGuiceSupport {
  def configure = {
      bind(classOf[Logger]).toInstance(Logger("avyeyes"))
      bindActor(classOf[DataMaintenance], "data-maintenance")
      bind(classOf[RuntimeEnvironment]).to(classOf[UserEnvironment])
      bind(classOf[Bootstrap]).asEagerSingleton()
  }
}
