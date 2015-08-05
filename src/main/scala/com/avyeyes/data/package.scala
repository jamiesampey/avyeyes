package com.avyeyes

import com.avyeyes.model.Avalanche
import com.avyeyes.util.Helpers._
import net.liftweb.util.Props
import org.postgresql.ds.PGSimpleDataSource
import slick.driver.{JdbcProfile, H2Driver, JdbcDriver, PostgresDriver}

import scala.collection.concurrent.{Map => CMap, TrieMap}

package object data {

  trait DriverComponent {
    val driver: JdbcProfile
  }

  val postgresDataSource = {
    val dataSource = new PGSimpleDataSource
    dataSource.setServerName(Props.get("db.host").openOr("localhost"))
    dataSource.setPortNumber(Props.getInt("db.port").openOr(9999))
    dataSource.setDatabaseName(Props.get("db.name").openOr("public"))
    dataSource
  }


  val AllAvalanchesMap: CMap[String, Avalanche] = new TrieMap()
}
