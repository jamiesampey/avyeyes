package com.avyeyes.data

import com.avyeyes.model.Avalanche
import helpers.BaseSpec
import org.specs2.specification._
import play.api.Configuration

import scala.concurrent.Future


trait DatabaseTest extends BaseSpec with BeforeAll {

  protected val subject: AvyEyesDatabase

  implicit val ec = scala.concurrent.ExecutionContext.global

  protected val h2Configuration = Configuration(
    "slick.dbs.default.driver" -> "slick.driver.H2Driver$",
    "slick.dbs.default.db.driver" -> "org.h2.Driver",
    "slick.dbs.default.db.url" -> "jdbc:h2:mem:avyeyes_test_db;DB_CLOSE_DELAY=-1",
    "slick.dbs.default.db.user" -> "sa"
  )

  import slick.jdbc.H2Profile.api._

  def beforeAll: Unit = subject.execute(sqlu"DROP ALL OBJECTS;" >> createSchema).resolve

  private def createSchema = (
    subject.AvalancheRows.schema ++
    subject.AvalancheWeatherRows.schema ++
    subject.AvalancheClassificationRows.schema ++
    subject.AvalancheHumanRows.schema ++
    subject.AvalancheImageRows.schema ++
    subject.AppUserRows.schema ++
    subject.AppUserRoleAssignmentRows.schema
  ).create

  protected def insertAvalanches(avalanches: Avalanche*)(implicit dao: CachedDao): Unit = Future.sequence(avalanches.toList.map(dao.insertAvalanche)).resolve
}

