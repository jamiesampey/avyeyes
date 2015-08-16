package com.avyeyes.data

import com.avyeyes.model.Avalanche
import com.avyeyes.service.{UserSession, Injectors}
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

  protected lazy val mockUserSession = mock[UserSession]
  protected lazy val cache = new TrieMap[String, Avalanche]()
  protected lazy val dal = new MemoryMapCachedDAL(H2Driver, h2DataSource, cache)

  def around[T: AsResult](t: => T): Result = Injectors.user.doWith(mockUserSession) {
    cache.clear()
    Await.result( Database.forDataSource(h2DataSource).run {
      sqlu"DROP ALL OBJECTS;" >> dal.createSchema }, 10 seconds)

     AsResult(t)
  }
}
