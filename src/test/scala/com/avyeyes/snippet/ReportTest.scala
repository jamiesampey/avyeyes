package com.avyeyes.snippet

import bootstrap.liftweb.Boot
import com.avyeyes.model.Avalanche
import com.avyeyes.model.StringSerializers._
import com.avyeyes.model.enums._
import com.avyeyes.service.Injectors
import com.avyeyes.test.Generators._
import com.avyeyes.test._
import com.avyeyes.util.Converters._
import net.liftweb.http.S
import net.liftweb.util.Mailer._
import org.mockito.ArgumentCaptor
import org.specs2.execute.Result

class ReportTest extends WebSpec2(Boot().boot _) with MockInjectors with TemplateReader {
  "Snippet rendering" should {
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
      assertInputValue(HiddenInputType, "rwAvyFormSky", report.sky)
      assertInputValue(HiddenInputType, "rwAvyFormPrecip", report.precip)
      assertInputValue(TextInputType, "rwAvyFormElevation", report.elevation)
      assertInputValue(HiddenInputType, "rwAvyFormAspect", report.aspect)
      assertInputValue(TextInputType, "rwAvyFormAngle", report.angle)
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

  "Avalanche field validation" should {
    "Validate submitter email" withSFor "/" in {
      val report = newReportWithTestData()
      report.submitterEmail = "thedude"
      val jsCmd = report.validateFields()
      jsCmd.toJsCmd contains "Error"
      jsCmd.toJsCmd contains "rwAvyFormSubmitterEmail"
    }

    "Validate submitter experience level" withSFor "/" in {
      val report = newReportWithTestData()
      report.submitterExp = ""
      val jsCmd = report.validateFields()
      jsCmd.toJsCmd contains "Error"
      jsCmd.toJsCmd contains "rwAvyFormSubmitterExpAC"
    }

    "Validate area name" withSFor "/" in {
      val report = newReportWithTestData()
      report.areaName = ""
      val jsCmd = report.validateFields()
      jsCmd.toJsCmd contains "Error"
      jsCmd.toJsCmd contains "rwAvyFormAreaName"
    }

    "Validate avalanche date" withSFor "/" in {
      val report = newReportWithTestData()
      report.dateStr = "2345"
      val jsCmd = report.validateFields()
      jsCmd.toJsCmd contains "Error"
      jsCmd.toJsCmd contains "rwAvyFormDate"
    }

    "Validate avalanche aspect" withSFor "/" in {
      val report = newReportWithTestData()
      report.aspect = ""
      val jsCmd = report.validateFields()
      jsCmd.toJsCmd contains "Error"
      jsCmd.toJsCmd contains "rwAvyFormAspectAC"
    }

    "Validate avalanche slope angle" withSFor "/" in {
      val report = newReportWithTestData()
      report.angle = "0"
      val jsCmd = report.validateFields()
      jsCmd.toJsCmd contains "Error"
      jsCmd.toJsCmd contains "rwAvyFormAngle"
    }
  }

  "Avalanche insert" should {
    isolated 
    
    "Not allow empty strings for enum fields" withSFor "/" in {
      val avalancheArg: ArgumentCaptor[Avalanche] = ArgumentCaptor.forClass(classOf[Avalanche]);
      
      val report = newReportWithTestData()
      report.sky = ""
      report.precip = ""
      report.avyType = ""
      report.avyTrigger = ""
      report.avyInterface = ""
      report.modeOfTravel = ""

      mockAvalancheDal.getAvalanche(report.extId) returns None
      
      report.saveReport()
      
      there was one(mockAvalancheDal).insertAvalanche(avalancheArg.capture())
      val passedAvalanche = avalancheArg.getValue
      
      passedAvalanche.scene.skyCoverage mustEqual SkyCoverage.U
      passedAvalanche.scene.precipitation mustEqual Precipitation.U
      passedAvalanche.classification.avyType mustEqual AvalancheType.U
      passedAvalanche.classification.trigger mustEqual AvalancheTrigger.U
      passedAvalanche.classification.interface mustEqual AvalancheInterface.U
      passedAvalanche.humanNumbers.modeOfTravel mustEqual ModeOfTravel.U
    }
    
    "Insert an avalanche with the correct values" withSFor "/" in {
      val avalancheArg = capture[Avalanche]

      val report = newReportWithTestData()
      mockAvalancheDal.getAvalanche(report.extId) returns None
      
      report.saveReport()
      
      there was one(mockAvalancheDal).insertAvalanche(avalancheArg)
      val passedAvalanche = avalancheArg.value
      
      passedAvalanche.extId mustEqual report.extId
      passedAvalanche.viewable must beFalse
      passedAvalanche.submitterEmail mustEqual report.submitterEmail
      passedAvalanche.submitterExp mustEqual ExperienceLevel.withCode(report.submitterExp)
      passedAvalanche.location.longitude mustEqual strToDblOrZero(report.lng)
      passedAvalanche.location.latitude mustEqual strToDblOrZero(report.lat)
      passedAvalanche.areaName mustEqual report.areaName
      passedAvalanche.date mustEqual strToDate(report.dateStr)
      passedAvalanche.scene.skyCoverage mustEqual SkyCoverage.withCode(report.sky)
      passedAvalanche.scene.precipitation mustEqual Precipitation.withCode(report.precip)
      passedAvalanche.classification.avyType mustEqual AvalancheType.withCode(report.avyType)
      passedAvalanche.classification.trigger mustEqual AvalancheTrigger.withCode(report.avyTrigger)
      passedAvalanche.classification.interface mustEqual AvalancheInterface.withCode(report.avyInterface)
      passedAvalanche.humanNumbers.caught mustEqual strToIntOrNegOne(report.caught)
      passedAvalanche.humanNumbers.partiallyBuried mustEqual strToIntOrNegOne(report.partiallyBuried)
      passedAvalanche.humanNumbers.fullyBuried mustEqual strToIntOrNegOne(report.fullyBuried)
      passedAvalanche.humanNumbers.injured mustEqual strToIntOrNegOne(report.injured)
      passedAvalanche.humanNumbers.killed mustEqual strToIntOrNegOne(report.killed)
      passedAvalanche.humanNumbers.modeOfTravel mustEqual ModeOfTravel.withCode(report.modeOfTravel)
      passedAvalanche.comments.get mustEqual report.comments
      passedAvalanche.perimeter mustEqual report.coordStr.trim.split(" ").toList.map(stringToCoordinate)
    }
    
    "Handles an insertion success correctly" withSFor "/" in {
      val report = spy(newReportWithTestData())
      val unreserveThisExtId = "4iu2kjr2"
      report.extId = unreserveThisExtId
      mockAvalancheDal.getAvalanche(report.extId) returns None
      
      val jsCmd = report.saveReport()

      there was one(report).unreserveExtId(unreserveThisExtId)
      jsCmd.toJsCmd must contain(report.extId)
    }
    
    "Handles an insertion failure correctly" withSFor "/" in {
      val exceptionMsg = "gotcha!"
      mockAvalancheDal.insertAvalanche(any[Avalanche]) throws new RuntimeException(exceptionMsg)
      
      val report = spy(newReportWithTestData())
      val unreserveThisExtId = "4iu2kjr2"
      report.extId = unreserveThisExtId
      mockAvalancheDal.getAvalanche(report.extId) returns None
      
      val jsCmd = report.saveReport()
      
      there was one(report).unreserveExtId(unreserveThisExtId)
      jsCmd.toJsCmd must contain("Error")
      jsCmd.toJsCmd must contain(S.?("msg.avyReportSaveError"))
    }
  }

  "Report email notifications" should {
    val R = Injectors.resources.vend

    "Send email to both submitter and admin upon initial report submission" withSFor "/" in {
      val fromArg = capture[From]
      val subjectArg = capture[Subject]
      val report = spy(newReportWithTestData())
      mockAvalancheDal.getAvalanche(report.extId) returns None

      report.saveReport()
      there was two(report).sendMail(fromArg, subjectArg, any[MailTypes])

      fromArg.values.get(0) mustEqual report.adminEmailFrom
      subjectArg.values.get(0).subject mustEqual
        R.getMessage("avyReportSubmitEmailAdminSubject", report.submitterEmail).toString

      fromArg.values.get(1) mustEqual report.adminEmailFrom
      subjectArg.values.get(1).subject mustEqual
        R.getMessage("avyReportSubmitEmailSubmitterSubject", report.extId).toString
    }

    "Send email to submitter upon report approval" withSFor "/" in {
      val fromArg = capture[From]
      val subjectArg = capture[Subject]
      val report = spy(newReportWithTestData())
      report.viewable = true
      val unapprovedAvalanche = avalancheForTest.copy(extId = report.extId, viewable = false)
      mockAvalancheDal.getAvalanche(report.extId) returns Some(unapprovedAvalanche)

      report.saveReport()
      there was one(report).sendMail(fromArg, subjectArg, any[MailTypes])

      fromArg.values.get(0) mustEqual report.adminEmailFrom
      subjectArg.values.get(0).subject mustEqual
        R.getMessage("avyReportApproveEmailSubmitterSubject", report.extId).toString
    }
  }

  private def newReportWithTestData(): Report = {
      val report = new Report
      
      report.extId = "jd3ru8vg"
      report.lat = "39.6634870900582"
      report.lng = "-105.875046142935"
      report.areaName = "east side of LP"
      report.dateStr = "01-27-2014"
      
      report.sky = SkyCoverage.BKN.toString
      report.precip = Precipitation.SN.toString
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
      report.coordStr = "-105.88914805,39.77335322,3783.54 -105.88942005,39.77336331,3781.82 -105.88943103,39.77336874,3781.69 -105.88948532,39.77340643,3781.04 -105.88949634,39.77341066,3780.87 -105.88952966,39.77341889,3780.35 -105.88954088,39.77341939,3780.17 -105.88955206,39.77342540,3780.18 -105.88958633,39.77342587,3780.00 -105.88965445,39.77343242,3779.54 -105.88968828,39.77343595,3779.11 -105.88979032,39.77342467,3777.26 -105.88985828,39.77342085,3776.00 -105.89001621,39.77338961,3771.01 -105.89010675,39.77337986,3768.33 -105.89026555,39.77334168,3762.15 -105.89034527,39.77332355,3758.90 -105.89051734,39.77327746,3751.79 -105.89057495,39.77326588,3749.47 -105.89063291,39.77323630,3746.81 -105.89069096,39.77319829,3743.68 -105.89079568,39.77315551,3738.49 -105.89086579,39.77311874,3734.61"
      
      report
  }
}