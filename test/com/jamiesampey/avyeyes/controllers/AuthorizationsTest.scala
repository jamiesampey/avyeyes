package com.jamiesampey.avyeyes.controllers

import com.jamiesampey.avyeyes.data.CachedDao
import com.jamiesampey.avyeyes.service.AvyEyesUserService._
import com.jamiesampey.avyeyes.service.ExternalIdService
import com.jamiesampey.avyeyes.util.Constants.AvalancheEditWindow
import helpers.BaseSpec
import org.joda.time.DateTime
import org.mockito.Mockito
import org.specs2.specification.BeforeEach
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder


class AuthorizationsTest extends BaseSpec with BeforeEach {

  private val testExtId = "49fk349d"

  private val mockDao = mock[CachedDao]
  private val mockExtIdService = mock[ExternalIdService]

  def before = {
    Mockito.reset(mockDao, mockExtIdService)
  }

  val appBuilder = new GuiceApplicationBuilder()
    .overrides(bind[ExternalIdService].toInstance(mockExtIdService))
    .overrides(bind[CachedDao].toInstance(mockDao))

  val injector = appBuilder.injector()
  val subject = injector.instanceOf[Authorizations]


  "View authorization" should {
    "allow an admin to view" in {
      val adminUser = genAvyEyesUser.generate.copy(roles = List(AdminRole))
      subject.isAuthorizedToView(testExtId, Some(adminUser)) must beTrue
    }

    "allow the site owner to view" in {
      val adminUser = genAvyEyesUser.generate.copy(roles = List(SiteOwnerRole))
      subject.isAuthorizedToView(testExtId, Some(adminUser)) must beTrue
    }

    "allow anyone to view a viewable avalanche" in {
      val existingAvalanche = genAvalanche.generate.copy(viewable = true)
      mockDao.getAvalanche(testExtId) returns Some(existingAvalanche)
      subject.isAuthorizedToView(testExtId, None) must beTrue
    }

    "disallow viewing of a non-viewable avalanche" in {
      val existingAvalanche = genAvalanche.generate.copy(viewable = false)
      mockDao.getAvalanche(testExtId) returns Some(existingAvalanche)
      subject.isAuthorizedToView(testExtId, None) must beFalse
    }
  }

  "Edit authorization" should {
    "allow an admin to edit" in {
      val adminUser = genAvyEyesUser.generate.copy(roles = List(AdminRole))
      subject.isAuthorizedToEdit(testExtId, Some(adminUser), None) must beTrue
    }

    "allow editing of a new avalanche report" in {
      mockExtIdService.reservationExists(testExtId) returns true
      subject.isAuthorizedToEdit(testExtId, None, None) must beTrue
    }

    "allow editing of an avalanche with an edit key" in {
      mockExtIdService.reservationExists(testExtId) returns false
      val existingAvalanche = genAvalanche.generate.copy(extId = testExtId, viewable = true, createTime = DateTime.now.minusDays(1))
      mockDao.getAvalanche(testExtId) returns Some(existingAvalanche)

      subject.isAuthorizedToEdit(testExtId, None, Some(existingAvalanche.editKey.toString)) must beTrue
    }

    "disallow editing of an avalanche after the edit window has expired" in {
      mockExtIdService.reservationExists(testExtId) returns false
      val existingAvalanche = genAvalanche.generate.copy(extId = testExtId, viewable = true, createTime = DateTime.now.minus(AvalancheEditWindow.toMillis + 10))
      mockDao.getAvalanche(testExtId) returns Some(existingAvalanche)

      subject.isAuthorizedToEdit(testExtId, None, Some(existingAvalanche.editKey.toString)) must beFalse
    }
  }
}
