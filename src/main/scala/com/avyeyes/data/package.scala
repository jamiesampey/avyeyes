package com.avyeyes

import com.avyeyes.model.Avalanche
import com.avyeyes.util.Helpers._
import org.postgresql.ds.PGSimpleDataSource
import slick.driver.PostgresDriver.api._

import scala.collection.concurrent.TrieMap
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.collection.concurrent.{Map => CMap}

package object data {

  val postgresDataSource = {
    val dataSource = new PGSimpleDataSource
    dataSource.setServerName(getProp("db.host"))
    dataSource.setPortNumber(getProp("db.port").toInt)
    dataSource.setDatabaseName(getProp("db.name"))
    dataSource
  }

  val AllAvalanchesMap: CMap[String, Avalanche] = new TrieMap()
  AllAvalanchesMap ++= Await.result(
    Database.forDataSource(postgresDataSource).run(DatabaseSchema.Avalanches.result).map {
      _.map(a => (a.extId, a.copy(comments = None))) }, Duration.Inf)

}
