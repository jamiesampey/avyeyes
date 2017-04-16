package com.avyeyes.system

import com.avyeyes.data.DataMaintenance
import com.google.inject.AbstractModule
import play.api.Logger
import play.libs.akka.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {
  def configure = {
      bind(classOf[Logger]).toInstance(Logger("avyeyes"))
      bindActor(classOf[DataMaintenance], "data-maintenance")
      bind(classOf[AvyEyesBootstrap]).asEagerSingleton()
  }
}
