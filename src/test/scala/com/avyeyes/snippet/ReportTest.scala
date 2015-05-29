package com.avyeyes.snippet

import net.liftweb.util.Mailer._

import scala.xml.NodeSeq
import org.mockito.ArgumentCaptor
import com.avyeyes.model.Avalanche
import com.avyeyes.model.enums._
import com.avyeyes.test._
import com.avyeyes.util.Helpers._
import bootstrap.liftweb.Boot
import net.liftweb.http.S

class ReportTest extends WebSpec2(Boot().boot _) with MockInjectors with TemplateReader {
  "Snippet rendering" should {
    "Wire input fields via CSS selectors" withSFor("/") in {

      val report = newReportWithTestData 
      val renderedPage = report.render(IndexHtmlElem)
      
      val HiddenInputType = "hidden"
      val TextInputType = "text"
      val TextareaInputType = "textarea"
      
      def assertInputValue(ns: NodeSeq, nodeType: String, cssSel: String, value: String) = {
        if (nodeType == TextareaInputType ) {
          val n = (ns \\ nodeType filter (node => (node\"@id").text == cssSel)).head
          n.text must_== value
        } else {
          val n = (ns \\ "input" filter (node => (node\"@type").text == nodeType && (node\"@id").text == cssSel)).head
          (n\"@value").text must_== value
        }
      }
      
      assertInputValue(renderedPage, HiddenInputType, "avyReportExtId", report.extId)
      assertInputValue(renderedPage, HiddenInputType, "avyReportLat", report.lat)
      assertInputValue(renderedPage, HiddenInputType, "avyReportLng", report.lng)
      assertInputValue(renderedPage, TextInputType, "avyReportAreaName", report.areaName)
      assertInputValue(renderedPage, TextInputType, "avyReportDate", report.dateStr)
      assertInputValue(renderedPage, HiddenInputType, "avyReportSky", report.sky)
      assertInputValue(renderedPage, HiddenInputType, "avyReportPrecip", report.precip)
      assertInputValue(renderedPage, TextInputType, "avyReportElevation", report.elevation)
      assertInputValue(renderedPage, HiddenInputType, "avyReportAspect", report.aspect)
      assertInputValue(renderedPage, TextInputType, "avyReportAngle", report.angle)
      assertInputValue(renderedPage, HiddenInputType, "avyReportType", report.avyType)
      assertInputValue(renderedPage, HiddenInputType, "avyReportTrigger", report.avyTrigger)
      assertInputValue(renderedPage, HiddenInputType, "avyReportInterface", report.avyInterface)
      assertInputValue(renderedPage, TextInputType, "avyReportRsizeValue", report.rSize)
      assertInputValue(renderedPage, TextInputType, "avyReportDsizeValue", report.dSize)
      assertInputValue(renderedPage, TextInputType, "avyReportNumCaught", report.caught)
      assertInputValue(renderedPage, TextInputType, "avyReportNumPartiallyBuried", report.partiallyBuried)
      assertInputValue(renderedPage, TextInputType, "avyReportNumFullyBuried", report.fullyBuried)
      assertInputValue(renderedPage, TextInputType, "avyReportNumInjured", report.injured)
      assertInputValue(renderedPage, TextInputType, "avyReportNumKilled", report.killed)
      assertInputValue(renderedPage, HiddenInputType, "avyReportModeOfTravel", report.modeOfTravel)
      assertInputValue(renderedPage, TextareaInputType, "avyReportComments", report.comments)
      assertInputValue(renderedPage, TextInputType, "avyReportSubmitterEmail", report.submitterEmail)
      assertInputValue(renderedPage, HiddenInputType, "avyReportSubmitterExp", report.submitterExp)
      assertInputValue(renderedPage, HiddenInputType, "avyReportKml", report.kmlStr)
    }
  }

  "Avalanche field validation" should {
    "Validate submitter email" withSFor("/") in {
      val report = newReportWithTestData
      report.submitterEmail = "thedude"
      val jsCmd = report.validateFields()
      jsCmd.toJsCmd contains("Error")
      jsCmd.toJsCmd contains("avyReportSubmitterEmail")
    }

    "Validate submitter experience level" withSFor("/") in {
      val report = newReportWithTestData
      report.submitterExp = ""
      val jsCmd = report.validateFields()
      jsCmd.toJsCmd contains("Error")
      jsCmd.toJsCmd contains("avyReportSubmitterExpAC")
    }

    "Validate area name" withSFor("/") in {
      val report = newReportWithTestData
      report.areaName = ""
      val jsCmd = report.validateFields()
      jsCmd.toJsCmd contains("Error")
      jsCmd.toJsCmd contains("avyReportAreaName")
    }

    "Validate avalanche date" withSFor("/") in {
      val report = newReportWithTestData
      report.dateStr = "2345"
      val jsCmd = report.validateFields()
      jsCmd.toJsCmd contains("Error")
      jsCmd.toJsCmd contains("avyReportDate")
    }

    "Validate avalanche aspect" withSFor("/") in {
      val report = newReportWithTestData
      report.aspect = ""
      val jsCmd = report.validateFields()
      jsCmd.toJsCmd contains("Error")
      jsCmd.toJsCmd contains("avyReportAspectAC")
    }

    "Validate avalanche slope angle" withSFor("/") in {
      val report = newReportWithTestData
      report.angle = "0"
      val jsCmd = report.validateFields()
      jsCmd.toJsCmd contains("Error")
      jsCmd.toJsCmd contains("avyReportAngle")
    }
  }

  "Avalanche insert" should {
    isolated 
    
    "Not allow empty strings for enum fields" withSFor("/") in {
      val avalancheArg: ArgumentCaptor[Avalanche] = ArgumentCaptor.forClass(classOf[Avalanche]);
      
      val report = newReportWithTestData
      report.sky = ""
      report.precip = ""
      report.avyType = ""
      report.avyTrigger = ""
      report.avyInterface = ""
      report.modeOfTravel = ""

      mockAvalancheDao.selectAvalanche(report.extId) returns None
      
      report.saveReport()
      
      there was one(mockAvalancheDao).insertAvalanche(avalancheArg.capture(), anyString)
      val passedAvalanche = avalancheArg.getValue
      
      passedAvalanche.sky must_== Sky.U
      passedAvalanche.precip must_== Precip.U
      passedAvalanche.avyType must_== AvalancheType.U
      passedAvalanche.avyTrigger must_== AvalancheTrigger.U
      passedAvalanche.avyInterface must_== AvalancheInterface.U
      passedAvalanche.modeOfTravel must_== ModeOfTravel.U
    }
    
    "Insert an avalanche with the correct values" withSFor("/") in {
      val avalancheArg: ArgumentCaptor[Avalanche] = ArgumentCaptor.forClass(classOf[Avalanche]);
      val emailArg: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String]);

      val report = newReportWithTestData
      mockAvalancheDao.selectAvalanche(report.extId) returns None
      
      report.saveReport()
      
      there was one(mockAvalancheDao).insertAvalanche(avalancheArg.capture(), emailArg.capture())
      val passedAvalanche = avalancheArg.getValue
      
      passedAvalanche.extId must_== report.extId
      passedAvalanche.viewable must beFalse
      passedAvalanche.submitterExp must_== ExperienceLevel.withName(report.submitterExp)
      passedAvalanche.lat must_== strToDblOrZero(report.lat)
      passedAvalanche.lng must_== strToDblOrZero(report.lng)
      passedAvalanche.areaName must_== report.areaName
      passedAvalanche.avyDate must_== strToDate(report.dateStr)
      passedAvalanche.sky must_== Sky.withName(report.sky)
      passedAvalanche.precip must_== Precip.withName(report.precip)
      passedAvalanche.avyType must_== AvalancheType.withName(report.avyType)
      passedAvalanche.avyTrigger must_== AvalancheTrigger.withName(report.avyTrigger)
      passedAvalanche.avyInterface must_== AvalancheInterface.withName(report.avyInterface)
      passedAvalanche.caught must_== strToIntOrNegOne(report.caught)
      passedAvalanche.partiallyBuried must_== strToIntOrNegOne(report.partiallyBuried)
      passedAvalanche.fullyBuried must_== strToIntOrNegOne(report.fullyBuried)
      passedAvalanche.injured must_== strToIntOrNegOne(report.injured)
      passedAvalanche.killed must_== strToIntOrNegOne(report.killed)
      passedAvalanche.modeOfTravel must_== ModeOfTravel.withName(report.modeOfTravel)
      passedAvalanche.comments must_== report.comments
      passedAvalanche.coords must_== testCoords
      emailArg.getValue must_== report.submitterEmail
    }
    
    "Handles an insertion success correctly" withSFor("/") in {
      val report = spy(newReportWithTestData)
      val unreserveThisExtId = "4iu2kjr2"
      report.extId = unreserveThisExtId
      mockAvalancheDao.selectAvalanche(report.extId) returns None
      
      val jsCmd = report.saveReport()

      there was one(report).unreserveExtId(unreserveThisExtId)
      jsCmd.toJsCmd must contain(report.extId)
    }
    
    "Handles an insertion failure correctly" withSFor("/") in {
      val exceptionMsg = "gotcha!"
      mockAvalancheDao.insertAvalanche(any[Avalanche], anyString) throws new RuntimeException(exceptionMsg)
      
      val report = spy(newReportWithTestData)
      val unreserveThisExtId = "4iu2kjr2"
      report.extId = unreserveThisExtId
      mockAvalancheDao.selectAvalanche(report.extId) returns None
      
      val jsCmd = report.saveReport()
      
      there was one(report).unreserveExtId(unreserveThisExtId)
      jsCmd.toJsCmd must contain("Error")
      jsCmd.toJsCmd must contain(S.?("msg.avyReportSaveError"))
    }
  }

  "Report email notifications" should {
    "Send email to both submitter and admin upon initial report submission" withSFor("/") in {
      val fromArg: ArgumentCaptor[From] = ArgumentCaptor.forClass(classOf[From]);
      val subjectArg: ArgumentCaptor[Subject] = ArgumentCaptor.forClass(classOf[Subject]);
      val report = spy(newReportWithTestData)
      mockAvalancheDao.selectAvalanche(report.extId) returns None

      report.saveReport()
      there was two(report).sendMail(fromArg.capture, subjectArg.capture, any[MailTypes])

      fromArg.getAllValues.get(0) must_== report.adminEmailFrom
      subjectArg.getAllValues.get(0).subject must_==
        getMessage("avyReportSubmitEmailAdminSubject", report.submitterEmail).toString

      fromArg.getAllValues.get(1) must_== report.adminEmailFrom
      subjectArg.getAllValues.get(1).subject must_==
        getMessage("avyReportSubmitEmailSubmitterSubject", report.extId).toString
    }

    "Send email to submitter upon report approval" withSFor("/") in {
      val fromArg: ArgumentCaptor[From] = ArgumentCaptor.forClass(classOf[From]);
      val subjectArg: ArgumentCaptor[Subject] = ArgumentCaptor.forClass(classOf[Subject]);
      val report = spy(newReportWithTestData)
      report.viewable = true
      mockAvalancheDao.selectAvalanche(report.extId) returns Some(new Avalanche)

      report.saveReport()
      there was one(report).sendMail(fromArg.capture, subjectArg.capture, any[MailTypes])

      fromArg.getAllValues.get(0) must_== report.adminEmailFrom
      subjectArg.getAllValues.get(0).subject must_==
        getMessage("avyReportApproveEmailSubmitterSubject", report.extId).toString
    }
  }

  private def newReportWithTestData(): Report = {
      val report = new Report
      
      report.extId = "jd3ru8vg"
      report.lat = "39.6634870900582"
      report.lng = "-105.875046142935"
      report.areaName = "east side of LP"
      report.dateStr = "01-27-2014"
      
      report.sky = Sky.Broken.toString
      report.precip = Precip.SN.toString
      report.elevation = "3500"
      report.aspect = Aspect.E.toString
      report.angle = "39"
      
      report.avyType = AvalancheType.HS.toString
      report.avyTrigger = AvalancheTrigger.AM.toString
      report.avyInterface = AvalancheInterface.O.toString
      report.rSize = "3.5"
      report.dSize = "4.0"
      
      report.caught = "3"
      report.partiallyBuried = "2"
      report.fullyBuried = "1"
      report.injured = "1"
      report.killed = "1"
      report.modeOfTravel = ModeOfTravel.Snowmobiler.toString

      report.comments = "some test comments here"
      report.submitterEmail = "sledhead@company.com"
      report.submitterExp = ExperienceLevel.A0.toString
      report.kmlStr = s"<kml><LinearRing><coordinates>$testCoords</coordinates></LinearRing></kml>"
      
      report
  }
  
  val testCoords = "-105.875489242241,39.66464854369643,3709.514235071098 -105.8754892458672,39.66464854070823,3709.514606327546 -105.8755061401399,39.66464732239427,3709.575492384985 -105.8755399183114,39.66464488490382,3709.696893243417 -105.8755736845457,39.66464244825151,3709.818665358297 -105.8756061647792,39.66460598711289,3708.469890683423"
}