package com.avyeyes.service

import akka.actor._
import com.avyeyes.persist.AvyEyesSqueryl._
import net.liftweb.common.Loggable

object DatabaseMaintainer {
  val PerformMaintenance = "performMaintenance"
}

class DatabaseMaintainer extends Actor with Loggable {
  lazy val dao = DependencyInjector.avalancheDao.vend

  def receive = {
    case DatabaseMaintainer.PerformMaintenance => {
      logger.info("Performing database maintenance")
      transaction {
        dao.performMaintenance()
      }
    }
    case _ => logger.error("Received unknown message")
  }
}
