package com.avyeyes.data

import com.avyeyes.model.Avalanche
import com.avyeyes.service.ExternalIdService
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.WithApplication

class AvalancheDaoTest extends DatabaseTest {

  private val mockExtIdService = mock[ExternalIdService]

  val appBuilder = new GuiceApplicationBuilder()
    .configure(h2Configuration)
    .overrides(bind[ExternalIdService].toInstance(mockExtIdService))
    .overrides(bind[DataMaintenance].toInstance(mock[DataMaintenance]))

  val injector = appBuilder.injector()
  implicit val subject = injector.instanceOf[AvalancheDao]

  "AvalancheDao" should {
    "read from the avalanches table" in new WithApplication(appBuilder.build) {
      val testAvalanche = genAvalanche.generate
      insertAvalanches(testAvalanche)
      val avalanches: Seq[Avalanche] = subject.getAvalanchesFromDisk.resolve
      avalanches must haveLength(1)
      avalanches.head.extId mustEqual testAvalanche.extId
    }
  }
}
