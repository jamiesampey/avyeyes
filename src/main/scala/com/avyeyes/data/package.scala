package com.avyeyes

import com.avyeyes.model.Avalanche
import com.avyeyes.util.Helpers._
import org.postgresql.ds.PGSimpleDataSource
import slick.driver.{H2Driver, JdbcDriver, PostgresDriver}

import scala.collection.concurrent.{Map => CMap, TrieMap}

package object data {

  object AgnosticDatabaseDriver {
    val api = profile.api
    lazy val profile: JdbcDriver = {
      sys.env.get("DB_ENVIRONMENT") match {
        case Some("h2") => H2Driver
        case _ => PostgresDriver
      }
    }
  }

  val postgresDataSource = {
    val dataSource = new PGSimpleDataSource
    dataSource.setServerName(getProp("db.host"))
    dataSource.setPortNumber(getProp("db.port").toInt)
    dataSource.setDatabaseName(getProp("db.name"))
    dataSource
  }

  val AllAvalanchesMap: CMap[String, Avalanche] = new TrieMap()
}
