package com.jamiesampey.avyeyes.data

import com.jamiesampey.avyeyes.service.ExternalIdService
import com.jamiesampey.avyeyes.util.Constants.AvalancheEditWindow
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.WithApplication

class AvalancheDaoWriteTest extends DatabaseTest {

  implicit val subject = injector.instanceOf[AvalancheDao]

  val moreThanEditWindow = AvalancheEditWindow.toMillis + 1000

  "Avalanche update" should {
    "Updates all modifiable fields" in new WithApplication(appBuilder.build) {
      val origAvalanche = genAvalanche.generate
      insertAvalanches(origAvalanche)

      val updatedAvalanche = genAvalanche.generate.copy(extId = origAvalanche.extId)
      subject.updateAvalanche(updatedAvalanche).resolve

      val result = subject.getAvalancheFromDisk(origAvalanche.extId).resolve.get
      result.createTime mustEqual origAvalanche.createTime
      result.viewable mustEqual updatedAvalanche.viewable
      result.submitterExp mustEqual updatedAvalanche.submitterExp
      result.submitterEmail mustEqual updatedAvalanche.submitterEmail
      result.location mustEqual origAvalanche.location
      result.areaName mustEqual updatedAvalanche.areaName
      result.date mustEqual updatedAvalanche.date
      result.weather mustEqual updatedAvalanche.weather
      result.slope.aspect mustEqual updatedAvalanche.slope.aspect
      result.slope.angle mustEqual updatedAvalanche.slope.angle
      result.slope.elevation mustEqual origAvalanche.slope.elevation
      result.classification mustEqual updatedAvalanche.classification
      result.comments mustEqual updatedAvalanche.comments
      result.perimeter mustEqual origAvalanche.perimeter
    }
  }

  "Avalanche delete" should {
    "works as expected" in new WithApplication(appBuilder.build) {
      val a1 = genAvalanche.generate
      val a2 = genAvalanche.generate
      insertAvalanches(a1, a2)

      subject.getAvalanche(a1.extId) must beSome
      subject.getAvalanche(a2.extId) must beSome

      subject.deleteAvalanche(a1.extId).resolve

      subject.getAvalanche(a1.extId) must beNone
      subject.getAvalanche(a2.extId) must beSome

      subject.deleteAvalanche(a2.extId).resolve

      subject.getAvalanche(a2.extId) must beNone
    }
  }
}
