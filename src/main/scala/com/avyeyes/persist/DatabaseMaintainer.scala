package com.avyeyes.persist

import akka.actor._
import net.liftweb.common.Loggable
import com.avyeyes.persist.AvyEyesSqueryl._

object DatabaseMaintainer {
  val PerformMaintenance = "performMaintenance"
}

class DatabaseMaintainer extends Actor with Loggable {
  lazy val dao: AvalancheDao = PersistenceInjector.avalancheDao.vend

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
