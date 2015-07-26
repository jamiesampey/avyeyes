package com.avyeyes.data

import com.avyeyes.util.UserSession
import org.specs2.execute._
import org.specs2.mock.Mockito
import org.specs2.specification._
import org.h2.jdbcx.JdbcDataSource
import AgnosticDatabaseDriver.api._

trait InMemoryDB extends AroundExample with Mockito {
  val h2DataSource = {
    val dataSource = new JdbcDataSource
    dataSource.setURL("jdbc:h2:mem:avyeyes_db;DB_CLOSE_DELAY=-1")
    dataSource.setUser("sa")
    dataSource
  }

  val Authorized = mock[UserSession]
  Authorized.isAuthorizedSession() returns true

  val NotAuthorized = mock[UserSession]
  NotAuthorized.isAuthorizedSession() returns false


  def recreateDB() = {

  }
  
  def around[T: AsResult](t: => T): Result = {
    recreateDB
    AsResult(t)
  }
}