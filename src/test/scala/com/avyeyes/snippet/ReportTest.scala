package com.avyeyes.snippet

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.{Coordinate, Avalanche}
import com.avyeyes.model.enums._
import com.avyeyes.service.{AmazonS3ImageService, Injectors, ResourceService}
import com.avyeyes.test.Generators._
import com.avyeyes.test._
import com.avyeyes.util.Converters._
import net.liftweb.util.Mailer._
import org.mockito.Matchers
import org.specs2.execute.{AsResult, Result}
import org.mockito.Matchers._
import org.specs2.mock.Mockito
import org.specs2.specification.AroundExample

import scala.xml.Unparsed

class ReportTest extends WebSpec2 with AroundExample with Mockito with TemplateReader {
  val mockResources = mock[ResourceService]
  val mockAvalancheDal = mock[CachedDAL]
  val mockS3 = mock[AmazonS3ImageService]

  mockResources.getProperty("s3.imageBucket") returns "some-bucket"
  mockResources.getProperty("s3.fullaccess.accessKeyId") returns "3490griow"
  mockResources.getProperty("s3.fullaccess.secretAccessKey") returns "34ijgeij4"

  def around[T: AsResult](t: => T): Result =
    Injectors.resources.doWith(mockResources) {
      Injectors.dal.doWith(mockAvalancheDal) {
        Injectors.s3.doWith(mockS3) {
          AsResult(t)
        }
      }
    }

  "Snippet rendering" >> {
    "Wire input fields via CSS selectors" withSFor "/" in {

      val report = newReportWithTestData()
      val renderedPage = report.render(IndexHtmlElem)
      
      val HiddenInputType = "hidden"
      val TextInputType = "text"
      val TextareaInputType = "textarea"
      
      def assertInputValue(nodeType: String, cssSel: String, value: String): Result = {
        if (nodeType == TextareaInputType ) {
          val n = (renderedPage \\ nodeType filter (node => (node\"@id").text == cssSel)).head
          n.text mustEqual value
        } else {
          val n = (renderedPage \\ "input" filter (node => (node\"@type").text == nodeType && (node\"@id").text == cssSel)).head
          (n\"@value").text mustEqual value
        }
      }
      
      assertInputValue(HiddenInputType, "rwAvyFormExtId", report.extId)
      assertInputValue(HiddenInputType, "rwAvyFormLat", report.lat)
      assertInputValue(HiddenInputType, "rwAvyFormLng", report.lng)
      assertInputValue(TextInputType, "rwAvyFormAreaName", report.areaName)
      assertInputValue(TextInputType, "rwAvyFormDate", report.dateStr)
      assertInputValue(TextInputType, "rwAvyFormElevation", report.elevation)
      assertInputValue(HiddenInputType, "rwAvyFormAspect", report.aspect)
      assertInputValue(TextInputType, "rwAvyFormAngle", report.angle)
      assertInputValue(TextInputType, "rwAvyFormRecentSnow", report.recentSnow)
      assertInputValue(HiddenInputType, "rwAvyFormRecentWindDirection", report.recentWindDirection)
      assertInputValue(HiddenInputType, "rwAvyFormRecentWindSpeed", report.recentWindSpeed)
      assertInputValue(HiddenInputType, "rwAvyFormType", report.avyType)
      assertInputValue(HiddenInputType, "rwAvyFormTrigger", report.avyTrigger)
      assertInputValue(HiddenInputType, "rwAvyFormInterface", report.avyInterface)
      assertInputValue(TextInputType, "rwAvyFormRsizeValue", report.rSize)
      assertInputValue(TextInputType, "rwAvyFormDsizeValue", report.dSize)
      assertInputValue(TextInputType, "rwAvyFormNumCaught", report.caught)
      assertInputValue(TextInputType, "rwAvyFormNumPartiallyBuried", report.partiallyBuried)
      assertInputValue(TextInputType, "rwAvyFormNumFullyBuried", report.fullyBuried)
      assertInputValue(TextInputType, "rwAvyFormNumInjured", report.injured)
      assertInputValue(TextInputType, "rwAvyFormNumKilled", report.killed)
      assertInputValue(HiddenInputType, "rwAvyFormModeOfTravel", report.modeOfTravel)
      assertInputValue(TextareaInputType, "rwAvyFormComments", report.comments)
      assertInputValue(TextInputType, "rwAvyFormSubmitterEmail", report.submitterEmail)
      assertInputValue(HiddenInputType, "rwAvyFormSubmitterExp", report.submitterExp)
      assertInputValue(HiddenInputType, "rwAvyFormCoords", report.coordStr)
    }
  }

  "Avalanche field validation" >> {
    "Validate submitter email" >> {
      val report = newReportWithTestData()
      report.submitterEmail = "thedude"
      val jsCmd = report.validateFields()
      jsCmd.toJsCmd contains "Error"
      jsCmd.toJsCmd contains "rwAvyFormSubmitterEmail"
    }

    "Validate submitter experience level" >> {
      val report = newReportWithTestData()
      report.submitterExp = ""
      val jsCmd = report.validateFields()
      jsCmd.toJsCmd contains "Error"
      jsCmd.toJsCmd contains "rwAvyFormSubmitterExpAC"
    }

    "Validate area name" >> {
      val report = newReportWithTestData()
      report.areaName = ""
      val jsCmd = report.validateFields()
      jsCmd.toJsCmd contains "Error"
      jsCmd.toJsCmd contains "rwAvyFormAreaName"
    }

    "Validate avalanche date" >> {
      val report = newReportWithTestData()
      report.dateStr = "2345"
      val jsCmd = report.validateFields()
      jsCmd.toJsCmd contains "Error"
      jsCmd.toJsCmd contains "rwAvyFormDate"
    }

    "Validate avalanche aspect" >> {
      val report = newReportWithTestData()
      report.aspect = ""
      val jsCmd = report.validateFields()
      jsCmd.toJsCmd contains "Error"
      jsCmd.toJsCmd contains "rwAvyFormAspectAC"
    }

    "Validate avalanche slope angle" >> {
      val report = newReportWithTestData()
      report.angle = "0"
      val jsCmd = report.validateFields()
      jsCmd.toJsCmd contains "Error"
      jsCmd.toJsCmd contains "rwAvyFormAngle"
    }
  }

  "Avalanche insert" >> {
    isolated 
    
    "Not allow empty strings for enum fields" >> {
      val avalancheArg = capture[Avalanche]
      
      val report = newReportWithTestData()
      report.aspect = ""
      report.recentWindDirection = ""
      report.recentWindSpeed = ""
      report.avyType = ""
      report.avyTrigger = ""
      report.avyInterface = ""
      report.modeOfTravel = ""

      mockAvalancheDal.getAvalanche(report.extId) returns None
      
      report.saveReport()
      
      there was one(mockAvalancheDal).insertAvalanche(avalancheArg)
      val passedAvalanche = avalancheArg.value

      passedAvalanche.slope.aspect mustEqual Direction.empty
      passedAvalanche.weather.recentWindDirection mustEqual Direction.empty
      passedAvalanche.weather.recentWindSpeed mustEqual WindSpeed.empty
      passedAvalanche.classification.avyType mustEqual AvalancheType.empty
      passedAvalanche.classification.trigger mustEqual AvalancheTrigger.empty
      passedAvalanche.classification.interface mustEqual AvalancheInterface.empty
      passedAvalanche.humanNumbers.modeOfTravel mustEqual ModeOfTravel.empty
    }
    
    "Insert an avalanche with the correct values" >> {
      val avalancheArg = capture[Avalanche]

      val report = newReportWithTestData()
      mockAvalancheDal.getAvalanche(report.extId) returns None
      
      report.saveReport()
      
      there was one(mockAvalancheDal).insertAvalanche(avalancheArg)
      val passedAvalanche = avalancheArg.value
      
      passedAvalanche.extId mustEqual report.extId
      passedAvalanche.viewable must beFalse
      passedAvalanche.submitterEmail mustEqual report.submitterEmail
      passedAvalanche.submitterExp mustEqual ExperienceLevel.fromCode(report.submitterExp)
      passedAvalanche.location.longitude mustEqual strToDblOrZero(report.lng)
      passedAvalanche.location.latitude mustEqual strToDblOrZero(report.lat)
      passedAvalanche.areaName mustEqual report.areaName
      passedAvalanche.date mustEqual strToDate(report.dateStr)
      passedAvalanche.weather.recentSnow mustEqual strToIntOrNegOne(report.recentSnow)
      passedAvalanche.weather.recentWindDirection mustEqual Direction.fromCode(report.recentWindDirection)
      passedAvalanche.weather.recentWindSpeed mustEqual WindSpeed.fromCode(report.recentWindSpeed)
      passedAvalanche.classification.avyType mustEqual AvalancheType.fromCode(report.avyType)
      passedAvalanche.classification.trigger mustEqual AvalancheTrigger.fromCode(report.avyTrigger)
      passedAvalanche.classification.interface mustEqual AvalancheInterface.fromCode(report.avyInterface)
      passedAvalanche.humanNumbers.caught mustEqual strToIntOrNegOne(report.caught)
      passedAvalanche.humanNumbers.partiallyBuried mustEqual strToIntOrNegOne(report.partiallyBuried)
      passedAvalanche.humanNumbers.fullyBuried mustEqual strToIntOrNegOne(report.fullyBuried)
      passedAvalanche.humanNumbers.injured mustEqual strToIntOrNegOne(report.injured)
      passedAvalanche.humanNumbers.killed mustEqual strToIntOrNegOne(report.killed)
      passedAvalanche.humanNumbers.modeOfTravel mustEqual ModeOfTravel.fromCode(report.modeOfTravel)
      passedAvalanche.comments.getOrElse("") mustEqual report.comments
      passedAvalanche.perimeter mustEqual report.coordStr.trim.split(" ").toList.map(Coordinate.fromString)
    }
    
    "Handles an insertion success correctly" >> {
      val successMsg = "The report was saved"
      val report = spy(newReportWithTestData())
      val extId = report.extId

      mockResources.localizedString(Matchers.eq("msg.avyReportInsertSuccess"), anyVararg()) returns successMsg
      mockResources.getAvalancheUrl(anyString) returns s"https://avyeyes.com/$extId"
      mockResources.localizedStringAsXml(anyString, anyVararg()) returns Unparsed("test xml")

      mockAvalancheDal.getAvalanche(extId) returns None
      
      val jsCmd = report.saveReport()

      there was one(report).unreserveExtId(extId)
      jsCmd.toJsCmd must contain(successMsg)
      jsCmd.toJsCmd must contain("avyEyesView.resetView")
    }
    
    "Handles an insertion failure correctly" >> {
      val errorMsg = "something bad happened"
      mockResources.localizedString("msg.avyReportSaveError") returns errorMsg

      val exceptionMsg = "gotcha!"
      mockAvalancheDal.insertAvalanche(any[Avalanche]) throws new RuntimeException(exceptionMsg)
      
      val report = spy(newReportWithTestData())
      val extId = report.extId
      mockAvalancheDal.getAvalanche(report.extId) returns None
      
      val jsCmd = report.saveReport()
      
      there was one(report).unreserveExtId(extId)
      jsCmd.toJsCmd must contain(errorMsg)
      jsCmd.toJsCmd must contain("avyEyesView.resetView")
    }
  }

  "Report email notifications" >> {

    "Send email to both submitter and admin upon initial report submission" >> {
      val testAdminSubject = "Someone submitted a report"
      val testSubmitterSubject = "Thanks for the report"

      mockResources.localizedString(Matchers.eq("msg.avyReportSubmitEmailAdminSubject"), anyVararg()) returns testAdminSubject
      mockResources.localizedString(Matchers.eq("msg.avyReportSubmitEmailSubmitterSubject"), anyVararg()) returns testSubmitterSubject

      val fromArg = capture[From]
      val subjectArg = capture[Subject]
      val report = spy(newReportWithTestData())
      mockAvalancheDal.getAvalanche(report.extId) returns None

      report.saveReport()
      there was two(report).sendMail(fromArg, subjectArg, any[MailTypes])

      fromArg.values.get(0) mustEqual report.adminEmailFrom
      subjectArg.values.get(0).subject mustEqual testAdminSubject

      fromArg.values.get(1) mustEqual report.adminEmailFrom
      subjectArg.values.get(1).subject mustEqual testSubmitterSubject
    }

    "Send email to submitter upon report approval" >> {
      val testApprovalSubject = "your report has been approved"
      mockResources.localizedString(Matchers.eq("msg.avyReportApproveEmailSubmitterSubject"), anyVararg()) returns testApprovalSubject

      val fromArg = capture[From]
      val subjectArg = capture[Subject]
      val report = spy(newReportWithTestData())
      report.viewable = true
      val unapprovedAvalanche = avalancheForTest.copy(extId = report.extId, viewable = false)
      mockAvalancheDal.getAvalanche(report.extId) returns Some(unapprovedAvalanche)

      report.saveReport()
      there was one(report).sendMail(fromArg, subjectArg, any[MailTypes])

      fromArg.values.get(0) mustEqual report.adminEmailFrom
      subjectArg.values.get(0).subject mustEqual testApprovalSubject
    }
  }

  private def newReportWithTestData(): Report = {
    val avalanche = Generators.avalancheForTest
    val report = new Report
      
    report.extId = avalanche.extId
    report.lat = avalanche.location.latitude.toString
    report.lng = avalanche.location.longitude.toString
    report.areaName = avalanche.areaName
    report.dateStr = dateToStr(avalanche.date)

    report.recentSnow = avalanche.weather.recentSnow.toString
    report.recentWindDirection = avalanche.weather.recentWindDirection.toString
    report.recentWindSpeed = avalanche.weather.recentWindSpeed.toString

    report.elevation = avalanche.slope.elevation.toString
    report.aspect = avalanche.slope.aspect.toString
    report.angle = avalanche.slope.angle.toString

    report.avyType = avalanche.classification.avyType.toString
    report.avyTrigger = avalanche.classification.trigger.toString
    report.avyInterface = avalanche.classification.interface.toString
    report.rSize = avalanche.classification.rSize.toString
    report.dSize = avalanche.classification.dSize.toString

    report.caught = avalanche.humanNumbers.caught.toString
    report.partiallyBuried = avalanche.humanNumbers.partiallyBuried.toString
    report.fullyBuried = avalanche.humanNumbers.fullyBuried.toString
    report.injured = avalanche.humanNumbers.injured.toString
    report.killed = avalanche.humanNumbers.killed.toString
    report.modeOfTravel = avalanche.humanNumbers.modeOfTravel.toString

    report.comments = avalanche.comments getOrElse ""
    report.submitterEmail = avalanche.submitterEmail
    report.submitterExp = avalanche.submitterExp.toString
    report.coordStr = avalanche.perimeter.map(coord => coord.toString).mkString(" ")

    report
  }
}