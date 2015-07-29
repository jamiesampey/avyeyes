package com.avyeyes.data

import com.avyeyes.data.AgnosticDatabaseDriver.api._
import com.avyeyes.data.DatabaseSchema._
import com.avyeyes.util.UserSession
import org.h2.jdbcx.JdbcDataSource
import org.specs2.execute._
import org.specs2.mock.Mockito
import org.specs2.specification._

import scala.collection.concurrent.TrieMap
import scala.concurrent.Await
import scala.concurrent.duration._


trait InMemoryDB extends AroundExample with Mockito {

  private val h2DataSource = {
    val dataSource = new JdbcDataSource
    dataSource.setURL("jdbc:h2:mem:avyeyes_db;DB_CLOSE_DELAY=-1")
    dataSource.setUser("sa")
    dataSource
  }

  val h2Db = Database.forDataSource(h2DataSource)

  val Authorized = mock[UserSession]
  Authorized.isAuthorizedSession() returns true

  val NotAuthorized = mock[UserSession]
  NotAuthorized.isAuthorizedSession() returns false

  def memoryMapCachedDaoForTest(userSession: UserSession) = 
    new MemoryMapCachedDao(h2DataSource, new TrieMap, userSession)

  private def newDatabase = {
    val schemas = Avalanches.schema ++ AvalancheImages.schema ++ Users.schema ++ UserRoles.schema
    h2Db.run { sqlu"DROP ALL OBJECTS;" >> schemas.create }
  }

  def around[T: AsResult](t: => T): Result = {
    Await.result(newDatabase, 30 seconds)
    AsResult(t)
  }
}