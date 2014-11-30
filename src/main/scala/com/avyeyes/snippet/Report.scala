package com.avyeyes.snippet

import com.avyeyes.model._
import com.avyeyes.model.enums._
import com.avyeyes.persist._
import com.avyeyes.service.ExternalIdService
import com.avyeyes.util.AEHelpers._
import com.avyeyes.util.{ReportNotifier, JsDialog}
import net.liftweb.common.Loggable
import net.liftweb.http.SHtml
import net.liftweb.http.js.JE.{Call, JsRaw}
import net.liftweb.http.js.JsCmd
import net.liftweb.json.JsonAST._
import net.liftweb.json.{JsonAST, Printer}
import net.liftweb.util.Helpers._
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils._
import org.squeryl.PrimitiveTypeMode.transaction

import scala.collection.mutable.ListBuffer
import scala.xml.XML

class Report extends ExternalIdService with Loggable {
  lazy val dao: AvalancheDao = PersistenceInjector.avalancheDao.vend
  
  var extId = ""; var viewable = false; var submitterEmail = ""; var submitterExp = "";
  var lat = ""; var lng = "";  var areaName = ""; var dateStr = ""; var sky = ""; var precip = ""
  var elevation = ""; var aspect = ""; var angle = ""    
  var avyType = ""; var avyTrigger = ""; var avyInterface = ""; var rSize = ""; var dSize = ""
  var caught = ""; var partiallyBuried = ""; var fullyBuried = ""; var injured = ""; var killed = ""
  var modeOfTravel = ""; var comments = ""; var kmlStr = ""
  
  def render = {
    "#avyReportExtId" #> SHtml.hidden(extId = _, extId) &
    "#avyReportLat" #> SHtml.hidden(lat = _, lat) &
    "#avyReportLng" #> SHtml.hidden(lng = _, lng) &
    "#avyReportViewable" #> SHtml.checkbox(viewable, (bool) => viewable = bool) &
    "#avyReportSubmitterEmail" #> SHtml.text(submitterEmail, submitterEmail = _) &
    "#avyReportSubmitterExp" #> SHtml.hidden(submitterExp = _, submitterExp) &
    "#avyReportAreaName" #> SHtml.text(areaName, areaName = _) &
    "#avyReportDate" #> SHtml.text(dateStr, dateStr = _) &
    "#avyReportSky" #> SHtml.hidden(sky = _, sky) &
    "#avyReportPrecip" #> SHtml.hidden(precip = _, precip) &
    "#avyReportElevation" #> SHtml.text(elevation, elevation = _) &
    "#avyReportAspect" #> SHtml.hidden(aspect = _, aspect) &
    "#avyReportAngle" #> SHtml.text(angle, angle = _) &
    "#avyReportType" #> SHtml.hidden(avyType = _, avyType) & 
    "#avyReportTrigger" #> SHtml.hidden(avyTrigger = _, avyTrigger) &
    "#avyReportInterface" #> SHtml.hidden(avyInterface = _, avyInterface) &
    "#avyReportRsizeValue" #> SHtml.text(rSize, rSize = _) &
    "#avyReportDsizeValue" #> SHtml.text(dSize, dSize = _) &
    "#avyReportNumCaught" #> SHtml.text(caught, caught = _) &
    "#avyReportNumPartiallyBuried" #> SHtml.text(partiallyBuried, partiallyBuried = _) &
    "#avyReportNumFullyBuried" #> SHtml.text(fullyBuried, fullyBuried = _) &
    "#avyReportNumInjured" #> SHtml.text(injured, injured = _) &
    "#avyReportNumKilled" #> SHtml.text(killed, killed = _) &
    "#avyReportModeOfTravel" #> SHtml.hidden(modeOfTravel = _, modeOfTravel) &
    "#avyReportComments" #> SHtml.textarea(comments, comments = _) &
    "#avyReportKml" #> SHtml.hidden(kmlStr = _, kmlStr) &
    "#avyReportSubmitBinding" #> SHtml.hidden(validateFields) &
    "#avyReportDeleteBinding [onClick]" #> SHtml.onEvent((value) => deleteReport(value))
  }

  def validateFields(): JsCmd = {
    val problemFields = new ListBuffer[String]
    if (!isValidEmail(submitterEmail)) problemFields.append("avyReportSubmitterEmail")
    if (!isValidEnumValue(ExperienceLevel, submitterExp)) problemFields.append("avyReportSubmitterExpAC")
    if (StringUtils.isBlank(areaName)) problemFields.append("avyReportAreaName")
    if (!isValidDate(dateStr)) problemFields.append("avyReportDate")
    if (!isValidEnumValue(Aspect, aspect)) problemFields.append("avyReportAspectAC")
    if (!isValidSlopeAngle(angle)) problemFields.append("avyReportAngle")

    if (problemFields.size == 0) {
      saveReport()
    } else {
      var problemFieldJsonArray = Printer.compact(
        JsonAST.render(JArray(problemFields.toList map(field => JString(field)))))
      JsRaw(s"avyeyes.currentReport.highlightValidationFields($problemFieldJsonArray)").cmd &
      JsDialog.error("avyReportValidationError")
    }
  }

  def saveReport(): JsCmd = {
    val avalancheFromValues = createAvalancheFromValues
    val jsDialogCmd = try {
      transaction {
        dao.selectAvalanche(extId) match {
          case Some(existingAvalanche) => {
            dao.updateAvalanche(avalancheFromValues)
            logger.info(s"Avalanche $extId successfully updated")
            if (!existingAvalanche.viewable && avalancheFromValues.viewable) {
              ReportNotifier.sendApprovalNotification(avalancheFromValues, submitterEmail)
            }
            JsDialog.info("avyReportUpdateSuccess")                
          }
          case None => {
            dao.insertAvalanche(avalancheFromValues, submitterEmail)
            logger.info(s"Avalanche $extId successfully inserted")
            ReportNotifier.sendSubmissionNotifications(avalancheFromValues, submitterEmail)
            JsDialog.info("avyReportInsertSuccess", getHttpBaseUrl + extId)  
          }
        }
      }
    } catch {
      case e: Exception => {
        logger.error(s"Error saving avalanche $extId", e)
        JsDialog.error("avyReportSaveError")
      }
    } finally {
      unreserveExtId(extId)
    }

    jsDialogCmd & Call("avyeyes.currentReport.finishReport").cmd
  }
  
  def deleteReport(extIdToDelete: String) = {
    try {
      transaction {
        dao.deleteAvalanche(extIdToDelete)
      }
      logger.info(s"Avalanche $extIdToDelete deleted")
      JsDialog.info("avyReportDeleteSuccess")  
    } catch {
      case e: Exception => {
        logger.error(s"Error deleting avalanche $extIdToDelete", e)
        JsDialog.error("avyReportDeleteError")
      }
    }
  }
  
  private def createAvalancheFromValues() = {
    val coords = kmlStr match {
      case str if (isNotBlank(str)) => (XML.loadString(str) \\ "LinearRing" \ "coordinates").head.text.trim
      case _ => ""
    }
    
    Avalanche(extId, viewable, ExperienceLevel.withName(submitterExp),
      strToDblOrZero(lat), strToDblOrZero(lng), areaName, strToDate(dateStr),
      enumWithNameOr(Sky, sky, Sky.U),
      enumWithNameOr(Precip, precip, Precip.U),
      strToIntOrNegOne(elevation), Aspect.withName(aspect), strToIntOrNegOne(angle),
      enumWithNameOr(AvalancheType, avyType, AvalancheType.U),
      enumWithNameOr(AvalancheTrigger, avyTrigger, AvalancheTrigger.U),
      enumWithNameOr(AvalancheInterface, avyInterface, AvalancheInterface.U),
      strToDblOrZero(rSize), strToDblOrZero(dSize),
      strToIntOrNegOne(caught), strToIntOrNegOne(partiallyBuried), strToIntOrNegOne(fullyBuried), 
      strToIntOrNegOne(injured), strToIntOrNegOne(killed), 
      enumWithNameOr(ModeOfTravel, modeOfTravel, ModeOfTravel.U),
      comments, coords)
  }
}
