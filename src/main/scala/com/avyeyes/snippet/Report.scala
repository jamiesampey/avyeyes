package com.avyeyes.snippet

import javax.mail.internet.MimeMessage
import javax.mail.{Authenticator, Multipart, PasswordAuthentication}

import com.avyeyes.data.DaoInjector
import com.avyeyes.model._
import com.avyeyes.model.enums._
import com.avyeyes.service.{AmazonS3ImageService, ExternalIdService}
import com.avyeyes.util.Helpers._
import com.avyeyes.util.JsDialog
import net.liftweb.common.{Full, Loggable}
import net.liftweb.http.SHtml
import net.liftweb.http.js.JE.{Call, JsRaw}
import net.liftweb.http.js.JsCmd
import net.liftweb.json.JsonAST._
import net.liftweb.json.{JsonAST, Printer}
import net.liftweb.util.Helpers._
import net.liftweb.util.Mailer._
import net.liftweb.util.{Mailer, Props}
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime

import scala.collection.mutable.ListBuffer

class Report extends ExternalIdService with Mailer with Loggable {
  lazy val dao = DaoInjector.dao.vend
  private val s3 = new AmazonS3ImageService

  val adminEmailFrom = From(getProp("mail.admin.address"), Full("Avy Eyes"))

  var extId = ""; var viewable = false; var submitterEmail = ""; var submitterExp = "";
  var lat = ""; var lng = "";  var areaName = ""; var dateStr = ""; var sky = ""; var precip = ""
  var elevation = ""; var aspect = ""; var angle = ""    
  var avyType = ""; var avyTrigger = ""; var avyInterface = ""; var rSize = ""; var dSize = ""
  var caught = ""; var partiallyBuried = ""; var fullyBuried = ""; var injured = ""; var killed = ""
  var modeOfTravel = ""; var comments = ""; var coordStr = ""
  
  def render = {
    "#rwAvyFormExtId" #> SHtml.hidden(extId = _, extId) &
    "#rwAvyFormLat" #> SHtml.hidden(lat = _, lat) &
    "#rwAvyFormLng" #> SHtml.hidden(lng = _, lng) &
    "#rwAvyFormViewable" #> SHtml.checkbox(viewable, (bool) => viewable = bool) &
    "#rwAvyFormSubmitterEmail" #> SHtml.text(submitterEmail, submitterEmail = _) &
    "#rwAvyFormSubmitterExp" #> SHtml.hidden(submitterExp = _, submitterExp) &
    "#rwAvyFormAreaName" #> SHtml.text(areaName, areaName = _) &
    "#rwAvyFormDate" #> SHtml.text(dateStr, dateStr = _) &
    "#rwAvyFormSky" #> SHtml.hidden(sky = _, sky) &
    "#rwAvyFormPrecip" #> SHtml.hidden(precip = _, precip) &
    "#rwAvyFormElevation" #> SHtml.text(elevation, elevation = _) &
    "#rwAvyFormAspect" #> SHtml.hidden(aspect = _, aspect) &
    "#rwAvyFormAngle" #> SHtml.text(angle, angle = _) &
    "#rwAvyFormType" #> SHtml.hidden(avyType = _, avyType) &
    "#rwAvyFormTrigger" #> SHtml.hidden(avyTrigger = _, avyTrigger) &
    "#rwAvyFormInterface" #> SHtml.hidden(avyInterface = _, avyInterface) &
    "#rwAvyFormRsizeValue" #> SHtml.text(rSize, rSize = _) &
    "#rwAvyFormDsizeValue" #> SHtml.text(dSize, dSize = _) &
    "#rwAvyFormNumCaught" #> SHtml.text(caught, caught = _) &
    "#rwAvyFormNumPartiallyBuried" #> SHtml.text(partiallyBuried, partiallyBuried = _) &
    "#rwAvyFormNumFullyBuried" #> SHtml.text(fullyBuried, fullyBuried = _) &
    "#rwAvyFormNumInjured" #> SHtml.text(injured, injured = _) &
    "#rwAvyFormNumKilled" #> SHtml.text(killed, killed = _) &
    "#rwAvyFormModeOfTravel" #> SHtml.hidden(modeOfTravel = _, modeOfTravel) &
    "#rwAvyFormComments" #> SHtml.textarea(comments, comments = _) &
    "#rwAvyFormCoords" #> SHtml.hidden(coordStr = _, coordStr) &
    "#rwAvyFormSubmitBinding" #> SHtml.hidden(validateFields) &
    "#rwAvyFormDeleteBinding [onClick]" #> SHtml.onEvent((value) => deleteReport(value))
  }

  def validateFields(): JsCmd = {
    val problemFields = new ListBuffer[String]
    if (!isValidEmail(submitterEmail)) problemFields.append("rwAvyFormSubmitterEmail")
    if (!isValidEnumValue(ExperienceLevel, submitterExp)) problemFields.append("rwAvyFormSubmitterExpAC")
    if (StringUtils.isBlank(areaName)) problemFields.append("rwAvyFormAreaName")
    if (!isValidDate(dateStr)) problemFields.append("rwAvyFormDate")
    if (!isValidEnumValue(Aspect, aspect)) problemFields.append("rwAvyFormAspectAC")
    if (!isValidSlopeAngle(angle)) problemFields.append("rwAvyFormAngle")

    if (problemFields.size == 0) {
      saveReport()
    } else {
      var problemFieldJsonArray = Printer.compact(
        JsonAST.render(JArray(problemFields.toList map(field => JString(field)))))
      JsRaw(s"avyEyesView.currentReport.highlightErrorFields($problemFieldJsonArray)").cmd &
      JsDialog.error("rwAvyFormValidationError")
    }
  }

  def saveReport(): JsCmd = {
    val avalancheFromValues = createAvalancheFromValues
    val jsDialogCmd = try {
      dao.getAvalanche(extId) match {
        case Some(existingAvalanche) => {
          dao.updateAvalanche(avalancheFromValues)
          logger.info(s"Avalanche $extId successfully updated")

          if (!existingAvalanche.viewable && avalancheFromValues.viewable) {
            sendApprovalNotification(avalancheFromValues, submitterEmail)
          }

          if (avalancheFromValues.viewable) {
            s3.allowPublicImageAccess(avalancheFromValues.extId)
          } else {
            s3.denyPublicImageAccess(avalancheFromValues.extId)
          }

          JsDialog.info("avyReportUpdateSuccess")
        }
        case None => {
          dao.insertAvalanche(avalancheFromValues)
          logger.info(s"Avalanche $extId successfully inserted")

          sendSubmissionNotifications(avalancheFromValues, submitterEmail)

          JsDialog.info("avyReportInsertSuccess", avalancheFromValues.getExtHttpUrl)
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

    jsDialogCmd & Call("avyEyesView.resetView").cmd
  }
  
  def deleteReport(extIdToDelete: String) = {
    try {
      dao.deleteAvalanche(extIdToDelete)

      s3.deleteAllImages(extIdToDelete)

      logger.info(s"Avalanche $extIdToDelete deleted")
      JsDialog.info("avyReportDeleteSuccess")  
    } catch {
      case e: Exception => {
        logger.error(s"Error deleting avalanche $extIdToDelete", e)
        JsDialog.error("avyReportDeleteError")
      }
    }
  }
  
  private def createAvalancheFromValues = {
    Avalanche(
      createTime = DateTime.now,
      updateTime = DateTime.now,
      extId = extId,
      viewable = viewable,
      submitterEmail = submitterEmail,
      submitterExp = ExperienceLevel.withName(submitterExp),
      location = Coordinate(strToDblOrZero(lng), strToDblOrZero(lat), strToIntOrNegOne(elevation)),
      areaName = areaName,
      date = strToDate(dateStr),
      scene = Scene(
        enumWithNameOr(SkyCoverage, sky, SkyCoverage.U),
        enumWithNameOr(Precipitation, precip, Precipitation.U)
      ),
      slope = Slope(
        Aspect.withName(aspect),
        strToIntOrNegOne(angle),
        strToIntOrNegOne(elevation)
      ),
      classification = Classification(
        avyType = enumWithNameOr(AvalancheType, avyType, AvalancheType.U),
        trigger = enumWithNameOr(AvalancheTrigger, avyTrigger, AvalancheTrigger.U),
        interface = enumWithNameOr(AvalancheInterface, avyInterface, AvalancheInterface.U),
        rSize = strToDblOrZero(rSize),
        dSize = strToDblOrZero(dSize)
      ),
      humanNumbers = HumanNumbers(
        modeOfTravel = enumWithNameOr(ModeOfTravel, modeOfTravel, ModeOfTravel.U),
        caught = strToIntOrNegOne(caught),
        partiallyBuried = strToIntOrNegOne(partiallyBuried),
        fullyBuried = strToIntOrNegOne(fullyBuried),
        injured = strToIntOrNegOne(injured),
        killed = strToIntOrNegOne(killed)
      ),
      comments = if (!comments.isEmpty) Some(comments) else None,
      perimeter = coordStr.trim.split(" ").toList.map(Coordinate.fromString)
    )
  }

  private def sendSubmissionNotifications(a: Avalanche, submitterEmail: String) = {
    configureMailer()

    val adminBody = getMessage("avyReportSubmitEmailAdminBody", submitterEmail, a.extId, a.getTitle, a.getExtHttpUrl)
    sendMail(adminEmailFrom, Subject(getMessage("avyReportSubmitEmailAdminSubject", submitterEmail).toString),
      (XHTMLMailBodyType(adminBody) :: To(adminEmailFrom.address) :: Nil) : _*)

    val submitterBody = getMessage("avyReportSubmitEmailSubmitterBody", a.extId, a.getExtHttpUrl)
    sendMail(adminEmailFrom, Subject(getMessage("avyReportSubmitEmailSubmitterSubject", a.extId).toString),
      (XHTMLMailBodyType(submitterBody) :: To(submitterEmail) :: Nil) : _*)
  }

  private def sendApprovalNotification(a: Avalanche, submitterEmail: String) = {
    configureMailer()

    val submitterBody = getMessage("avyReportApproveEmailSubmitterBody", a.getTitle, a.getExtHttpUrl)
    sendMail(adminEmailFrom, Subject(getMessage("avyReportApproveEmailSubmitterSubject", a.extId).toString),
      (XHTMLMailBodyType(submitterBody) :: To(submitterEmail) :: Nil) : _*)
  }

  private def configureMailer() = {
    customProperties = Map (
      "mail.smtp.host" -> getProp("mail.smtp.host"),
      "mail.smtp.port" -> getProp("mail.smtp.port"),
      "mail.smtp.auth" -> getProp("mail.smtp.auth"),
      "mail.smtp.starttls.enable" -> getProp("mail.smtp.starttls.enable")
    )

    if (Props.get("mail.smtp.auth", "false").toBoolean) {
      (Props.get("mail.admin.address"), Props.get("mail.admin.pw")) match {
        case (Full(username), Full(password)) =>
          authenticator = Full(new Authenticator() {
            override def getPasswordAuthentication = new PasswordAuthentication(username, password)
          })
        case _ => logger.error("Missing username and/or password for SMTP email")
      }
    }
  }

  devModeSend.default.set((m: MimeMessage) => {
    val multipartContent = m.getContent.asInstanceOf[Multipart]
    val firstBodyPartContent = multipartContent.getBodyPart(0).getDataHandler.getContent
    logger.info( s"""Dev mode report email:
         From: ${m.getFrom()(0).toString}
         To: ${m.getAllRecipients()(0).toString}
         Subject: ${m.getSubject}
         Content: ${firstBodyPartContent.asInstanceOf[String]}""")
  })
}
