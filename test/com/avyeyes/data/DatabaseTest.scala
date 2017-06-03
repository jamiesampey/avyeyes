package com.avyeyes.data

import com.avyeyes.model.Avalanche
import com.avyeyes.util.FutureOps._
import org.specs2.execute._
import org.specs2.mock.Mockito
import org.specs2.specification._
import play.api.db.Databases

import scala.concurrent.Future


trait DatabaseTest extends AroundEach with Mockito {

  protected val subject: AvyEyesDatabase

  implicit val executionContext = scala.concurrent.ExecutionContext.global

  import slick.jdbc.H2Profile.api._

//  protected lazy val cache = new TrieMap[String, Avalanche]()
//  protected lazy val dal = new MemoryMapCachedDAL(H2Driver, h2DataSource, cache)

  def around[T: AsResult](t: => T): Result = Databases.withInMemory(
      name = "avyeyesTestDb",
      urlOptions = Map("MODE" -> "PostgreSQL")
    ) { database =>
      createSchema
      try AsResult(t)
      finally database.shutdown()
    }

  private def createSchema = (
    subject.AvalancheRows.schema ++
    subject.AvalancheWeatherRows.schema ++
    subject.AvalancheClassificationRows.schema ++
    subject.AvalancheHumanRows.schema ++
    subject.AvalancheImageRows.schema ++
    subject.AppUserRows.schema ++
    subject.AppUserRoleAssignmentRows.schema
  ).create

  protected def insertAvalanches(avalanches: Avalanche*)(implicit dal: CachedDAL) = Future.sequence(avalanches.toList.map(dal.insertAvalanche)).resolve
}

