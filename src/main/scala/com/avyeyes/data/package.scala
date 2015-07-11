package com.avyeyes

import com.avyeyes.model.Avalanche
import com.avyeyes.util.Helpers._
import org.postgresql.ds.PGSimpleDataSource

import scala.collection.concurrent.{Map => CMap, TrieMap}

package object data {

  val postgresDataSource = {
    val dataSource = new PGSimpleDataSource
    dataSource.setServerName(getProp("db.host"))
    dataSource.setPortNumber(getProp("db.port").toInt)
    dataSource.setDatabaseName(getProp("db.name"))
    dataSource
  }

  val AllAvalanchesMap: CMap[String, Avalanche] = new TrieMap()
}
