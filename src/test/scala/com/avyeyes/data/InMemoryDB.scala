package com.avyeyes.data

import com.avyeyes.service.UserInjector
import com.avyeyes.util.UserSession
import org.h2.jdbcx.JdbcDataSource
import org.specs2.execute._
import org.specs2.mock.Mockito
import org.specs2.specification._
import slick.driver.H2Driver

import scala.collection.concurrent.TrieMap
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try


trait InMemoryDB extends AroundExample with Mockito {

  import H2Driver.api._

  private val h2DataSource = {
    val dataSource = new JdbcDataSource
    dataSource.setURL("jdbc:h2:mem:avyeyes_db;DB_CLOSE_DELAY=-1")
    dataSource.setUser("sa")
    dataSource
  }

  private val mockUserSession = mock[UserSession]
  private lazy val memoryMapCachedDAL =
    Try(new MemoryMapCachedDAL(H2Driver, h2DataSource, new TrieMap)) match {
      case scala.util.Success(instance) =>
        println("Successfully created a DAL")
        instance
      case scala.util.Failure(ex) =>
        println(s"Failed to create a DAL, ${ex.getMessage}"); null
    }

  val Authorized = true
  val NotAuthorized = false
  
  def memoryMapCachedDaoForTest(isAuthorized: Boolean) = {
    isAuthorized match {
      case true => mockUserSession.isAuthorizedSession() returns true
      case false => mockUserSession.isAuthorizedSession() returns false
    }
    memoryMapCachedDAL
  }

  def around[T: AsResult](t: => T): Result = {
    print(memoryMapCachedDAL)
    Await.result( Database.forDataSource(h2DataSource).run {
      sqlu"DROP ALL OBJECTS;" >> memoryMapCachedDAL.createSchema },
      10 seconds)

    UserInjector.userSession.doWith(mockUserSession) { AsResult(t) }
  }
}

