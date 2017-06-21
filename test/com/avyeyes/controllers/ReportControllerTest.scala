package com.avyeyes.controllers

import com.avyeyes.data.CachedDao
import com.avyeyes.model.Avalanche
import com.avyeyes.service.AvyEyesUserService.AdminRole
import com.avyeyes.service.{AmazonS3Service, ConfigurationService, ExternalIdService}
import com.avyeyes.util.Constants.AvalancheEditWindow
import helpers.BaseSpec
import org.joda.time.DateTime
import org.json4s.Extraction
import org.json4s.JsonAST.JString
import org.mockito.Mockito
import org.specs2.specification.BeforeEach
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsString
import play.api.test.{FakeRequest, WithApplication}
import securesocial.core.SecureSocial.RequestWithUser


class ReportControllerTest extends BaseSpec with BeforeEach with Json4sMethods {
  override val configService = mock[ConfigurationService]
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  private val testExtId = "49fk349d"

  private val mockDao = mock[CachedDao]
  private val mockS3Service = mock[AmazonS3Service]
  private val mockExtIdService = mock[ExternalIdService]

  def before = {
    Mockito.reset(mockDao, mockS3Service, mockExtIdService)
  }

  val appBuilder = new GuiceApplicationBuilder()
    .overrides(bind[ExternalIdService].toInstance(mockExtIdService))
    .overrides(bind[CachedDao].toInstance(mockDao))
    .overrides(bind[AmazonS3Service].toInstance(mockS3Service))

  val injector = appBuilder.injector()
  val subject = injector.instanceOf[ReportController]

  "Report submission" should {
    "retrieve a new external ID" in new WithApplication(appBuilder.build) {
      implicit val implicitDALForIdService = mockDao

      mockExtIdService.reserveNewExtId returns testExtId
      val requestWithUser = RequestWithUser(None, None, FakeRequest())

      val action = subject.newReportId()
      val result = call(action, requestWithUser)
      val jsonResponse = contentAsJson(result)

      there was one(mockExtIdService).reserveNewExtId
      result.resolve.header.status mustEqual OK
      (jsonResponse \ "extId").as[JsString].value mustEqual testExtId
    }

    "insert a new avalanche report" in new WithApplication(appBuilder.build) {
      val newAvalanche = genAvalanche.generate.copy(extId = testExtId)
      val newReportRequest = FakeRequest().withTextBody(testAvalancheJson(newAvalanche))

      val action = subject.submitReport(testExtId)
      val result = call(action, newReportRequest).resolve

      val avalancheArgCapture = capture[Avalanche]
      there was one(mockDao).insertAvalanche(avalancheArgCapture.capture)

      result.header.status mustEqual OK
      avalancheArgCapture.value.extId mustEqual newAvalanche.extId
      avalancheArgCapture.value.submitterEmail mustEqual newAvalanche.submitterEmail
      there was one(mockS3Service).allowPublicFileAccess(newAvalanche.extId)
    }
  }

  "Report update" should {
    "update an avalanche report with a valid edit key" in new WithApplication(appBuilder.build) {
      val originalAvalanche = genAvalanche.generate.copy(extId = testExtId, createTime = DateTime.now.minusDays(1))
      mockDao.getAvalanche(testExtId) returns Some(originalAvalanche)

      val newComments = "new comments"
      val updatedAvalanche = originalAvalanche.copy(comments = Some(newComments), viewable = true)

      val updateRequest = FakeRequest().withTextBody(testAvalancheJson(updatedAvalanche))

      val action = subject.updateReport(testExtId, Some(originalAvalanche.editKey.toString))
      val result = call(action, updateRequest).resolve

      val avalancheArgCapture = capture[Avalanche]
      there was one(mockDao).updateAvalanche(avalancheArgCapture.capture)

      result.header.status mustEqual OK
      avalancheArgCapture.value.extId mustEqual originalAvalanche.extId
      avalancheArgCapture.value.comments must beSome(newComments)
      avalancheArgCapture.value.createTime mustEqual originalAvalanche.createTime
      there was one(mockS3Service).allowPublicImageAccess(testExtId)
    }

    "disallow an update without an edit key" in new WithApplication(appBuilder.build) {
      val originalAvalanche = genAvalanche.generate.copy(extId = testExtId, createTime = DateTime.now.minusDays(1))
      mockDao.getAvalanche(testExtId) returns Some(originalAvalanche)

      val newComments = "new comments"
      val updatedAvalanche = originalAvalanche.copy(comments = Some(newComments), viewable = true)

      val updateRequest = FakeRequest().withTextBody(testAvalancheJson(updatedAvalanche))

      val action = subject.updateReport(testExtId, None)
      val result = call(action, updateRequest).resolve

      result.header.status mustEqual UNAUTHORIZED
      there was no(mockDao).updateAvalanche(any)
    }

    "disallow an update with an expired edit key" in new WithApplication(appBuilder.build) {
      val originalAvalanche = genAvalanche.generate.copy(extId = testExtId, createTime = DateTime.now.minus(AvalancheEditWindow.toMillis + 10))
      mockDao.getAvalanche(testExtId) returns Some(originalAvalanche)

      val newComments = "new comments"
      val updatedAvalanche = originalAvalanche.copy(comments = Some(newComments), viewable = true)

      val updateRequest = FakeRequest().withTextBody(testAvalancheJson(updatedAvalanche))

      val action = subject.updateReport(testExtId, Some(originalAvalanche.editKey.toString))
      val result = call(action, updateRequest).resolve

      result.header.status mustEqual UNAUTHORIZED
      there was no(mockDao).updateAvalanche(any)
    }
  }

  "Report delete" should {
    "disallow report deletion if the user is not an admin" in new WithApplication(appBuilder.build) {
      val deleteRequest = RequestWithUser(None, None, FakeRequest())

      val action = subject.deleteReport(testExtId)
      val result = call(action, deleteRequest).resolve

      result.header.status mustEqual UNAUTHORIZED
      there was no(mockDao).deleteAvalanche(any)
      there was no(mockS3Service).deleteAllFiles(any)
    }

    "delete a report if the user is an admin" in new WithApplication(appBuilder.build) {
      val adminUser = genAvyEyesUser.generate.copy(roles = List(AdminRole))

      val deleteRequest = RequestWithUser(Some(adminUser), None, FakeRequest())

      val action = subject.deleteReport(testExtId)
      val result = call(action, deleteRequest).resolve

//      result.header.status mustEqual OK
//      there was one(mockDAL).deleteAvalanche(testExtId)
//      there was one(mockS3Service).deleteAllFiles(testExtId)
      pending // figure out how to test SecureSocial.ws SecuredAction
    }
  }

  private def testAvalancheJson(avalanche: Avalanche) = writeJson(
    Extraction.decompose(avalanche).replace(List("perimeter"), JString(avalanche.perimeter.map(_.toString).mkString(" ")))
  )
}
