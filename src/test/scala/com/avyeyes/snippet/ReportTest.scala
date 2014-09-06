package com.avyeyes.snippet

import scala.xml.NodeSeq

import com.avyeyes.model.enums._
import com.avyeyes.test._

class ReportTest extends AvyEyesSpec {
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
      assertInputValue(renderedPage, HiddenInputType, "avyReportTrigger", report.trigger)
      assertInputValue(renderedPage, HiddenInputType, "avyReportBedSurface", report.bedSurface)
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
  
  private def newReportWithTestData(): Report = {
      val report = new Report
      
      report.extId = "jd3ru8vg"
      report.lat = "39.6634870900582"
      report.lng = "-105.875046142935"
      report.areaName = "east side of LP"
      report.dateStr = "01-27-2014"
      
      report.sky = Sky.BROKEN.toString
      report.precip = Precip.SN.toString
      report.elevation = "3500"
      report.aspect = Aspect.E.toString
      report.angle = "39"
      
      report.avyType = AvalancheType.HS.toString
      report.trigger = AvalancheTrigger.AM.toString
      report.bedSurface = AvalancheInterface.O.toString
      report.rSize = "3.5"
      report.dSize = "4.0"
      
      report.caught = "3"
      report.partiallyBuried = "2"
      report.fullyBuried = "1"
      report.injured = "1"
      report.killed = "1"
      report.modeOfTravel = ModeOfTravel.SNOWMOBILER.toString

      report.comments = "some test comments here"
      report.submitterEmail = "sledhead@company.com"
      report.submitterExp = ExperienceLevel.A0.toString
      report.kmlStr = "<kml>some coords and stuff</kml>"
      
      report
  }
  
}