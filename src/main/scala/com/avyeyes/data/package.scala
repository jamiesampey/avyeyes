package com.avyeyes

import com.avyeyes.model.Avalanche
import net.liftweb.util.Props
import org.postgresql.ds.PGSimpleDataSource
import slick.driver.JdbcProfile

import scala.collection.concurrent.{Map => CMap, TrieMap}

package object data {

  trait DriverComponent {
    val driver: JdbcProfile
  }

  val postgresDataSource = {
    val dataSource = new PGSimpleDataSource
    dataSource.setServerName(Props.get("db.host").openOr("localhost"))
    dataSource.setPortNumber(Props.getInt("db.port").openOr(9999))
    dataSource.setDatabaseName(Props.get("db.name").openOr(""))
    dataSource.setUser(Props.get("db.user").openOr(""))
    dataSource.setPassword(Props.get("db.pw").openOr(""))
    dataSource
  }

  val AllAvalanchesMap: CMap[String, Avalanche] = new TrieMap()
}
