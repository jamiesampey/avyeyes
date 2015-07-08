package com.avyeyes.data

import com.avyeyes.util.Helpers._
import org.postgresql.ds.PGSimpleDataSource
import slick.driver.PostgresDriver.api._

object PostgresSession {
  implicit def session = {
    val ds = new PGSimpleDataSource
    ds.setServerName(getProp("db.host"))
    ds.setPortNumber(getProp("db.port").toInt)
    ds.setDatabaseName(getProp("db.name"))

    val db = Database.forDataSource(ds)
    db.createSession
  }
}
