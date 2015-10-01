package com.avyeyes

import com.avyeyes.model.Avalanche
import com.avyeyes.service.Injectors
import org.postgresql.ds.PGSimpleDataSource
import slick.driver.JdbcProfile

import scala.collection.concurrent.{Map => CMap, TrieMap}

package object data {
  val R = Injectors.resources.vend

  trait DriverComponent {
    val driver: JdbcProfile
  }

  val PostgresDataSource = {
    val dataSource = new PGSimpleDataSource
    dataSource.setServerName(R.getProperty("db.host"))
    dataSource.setPortNumber(R.getIntProperty("db.port"))
    dataSource.setDatabaseName(R.getProperty("db.name"))
    dataSource.setUser(R.getProperty("db.user"))
    dataSource.setPassword(R.getProperty("db.pw"))
    dataSource
  }

  val AllAvalanchesMap: CMap[String, Avalanche] = new TrieMap()
}
