package com.avyeyes.system

import javax.inject.{Inject, Named, Singleton}

import akka.actor.{ActorRef, ActorSystem}
import com.avyeyes.data.DataMaintenance
import org.joda.time.{DateTime, Minutes}
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

@Singleton
class Bootstrap @Inject()(actorSystem: ActorSystem, logger: Logger, @Named("data-maintenance") dataMaintenanceActor: ActorRef) {
  logger.info("AvyEyes Startup")

  dataMaintenanceActor ! DataMaintenance.run // initial run at system startup

  private val startupTime = DateTime.now
  private val firstScheduledRun = startupTime.plusDays(1).withTime(2, 15, 0, 0)

  actorSystem.scheduler.schedule(
    initialDelay = Minutes.minutesBetween(startupTime, firstScheduledRun).getMinutes minutes,
    interval = 24 hours,
    receiver = dataMaintenanceActor,
    message = DataMaintenance.run)
}
