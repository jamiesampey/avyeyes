package com.avyeyes.data

import com.avyeyes.model.Avalanche
import com.avyeyes.service.ExternalIdService
import helpers.BaseSpec
import org.specs2.specification._
import play.api.Configuration
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Future


trait DatabaseTest extends BaseSpec with BeforeAll with BeforeEach {
  isolated

  protected implicit val ec = scala.concurrent.ExecutionContext.global

  protected val subject: AvyEyesDatabase

  def before: Unit = subject.deleteAllRows.resolve
  def beforeAll: Unit = subject.createSchema.resolve

  private val mockExtIdService = mock[ExternalIdService]

  protected val appBuilder = new GuiceApplicationBuilder()
    .overrides(bind[ExternalIdService].toInstance(mockExtIdService))
    .overrides(bind[DataMaintenance].toInstance(mock[DataMaintenance]))
    .configure(Configuration(
      "slick.dbs.default.driver" -> "slick.driver.H2Driver$",
      "slick.dbs.default.db.driver" -> "org.h2.Driver",
      "slick.dbs.default.db.url" -> "jdbc:h2:mem:avyeyes_test_db;DB_CLOSE_DELAY=-1",
      "slick.dbs.default.db.user" -> "sa"
    ))

  protected val injector = appBuilder.injector()

  protected def insertAvalanches(avalanches: Avalanche*)(implicit dao: CachedDao): Unit = Future.sequence(avalanches.toList.map(dao.insertAvalanche)).resolve
}

