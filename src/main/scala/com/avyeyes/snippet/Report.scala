package com.avyeyes.snippet

import javax.mail.internet.MimeMessage
import javax.mail.{Authenticator, Multipart, PasswordAuthentication}
import com.avyeyes.model._
import com.avyeyes.model.enums._
import com.avyeyes.service.{ExternalIdService, Injectors}
import com.avyeyes.util.Constants._
import com.avyeyes.util.Converters._
import com.avyeyes.util.Validators._
import net.liftweb.common.{Full, Loggable}
import net.liftweb.http.{S, SHtml}
import net.liftweb.http.js.JE.{Call, JsRaw}
import net.liftweb.http.js.JsCmd
import net.liftweb.json.JsonAST._
import net.liftweb.json.{JsonAST, Printer}
import net.liftweb.util.Helpers._
import net.liftweb.util.Mailer._
import net.liftweb.util.Mailer
import org.apache.commons.lang3.StringEscapeUtils._
import org.apache.commons.lang3.StringUtils._
import org.joda.time.DateTime

import scala.collection.mutable.ListBuffer

class Report extends ExternalIdService with ModalDialogs with Mailer with Loggable {
  val R = Injectors.resources.vend
  val dal = Injectors.dal.vend
  val s3 = Injectors.s3.vend
  val user = Injectors.user.vend

  val adminEmailFrom = From(R.getProperty("mail.admin.address"), Full("AvyEyes"))

  var extId = ""; var viewable = true; var submitterEmail = ""; var submitterExp = ""
  var lat = ""; var lng = "";  var areaName = ""; var dateStr = ""
  var recentSnow = ""; var recentWindSpeed = ""; var recentWindDirection = ""
  var elevation = ""; var aspect = ""; var angle = ""    
  var avyType = ""; var avyTrigger = ""; var avyTriggerModifier = ""; var avyInterface = ""; var rSize = ""; var dSize = ""
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
    "#rwAvyFormElevation" #> SHtml.text(elevation, elevation = _) &
    "#rwAvyFormAspect" #> SHtml.hidden(aspect = _, aspect) &
    "#rwAvyFormAngle" #> SHtml.text(angle, angle = _) &
    "#rwAvyFormRecentSnow" #> SHtml.text(recentSnow, recentSnow = _) &
    "#rwAvyFormRecentWindSpeed" #> SHtml.hidden(recentWindSpeed = _, recentWindSpeed) &
    "#rwAvyFormRecentWindDirection" #> SHtml.hidden(recentWindDirection = _, recentWindDirection) &
    "#rwAvyFormType" #> SHtml.hidden(avyType = _, avyType) &
    "#rwAvyFormTrigger" #> SHtml.hidden(avyTrigger = _, avyTrigger) &
    "#rwAvyFormTriggerModifier" #> SHtml.hidden(avyTriggerModifier = _, avyTriggerModifier) &
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
    if (!ExperienceLevel.isValidCode(submitterExp)) problemFields.append("rwAvyFormSubmitterExpAC")
    if (isBlank(areaName)) problemFields.append("rwAvyFormAreaName")
    if (!isValidDate(dateStr)) problemFields.append("rwAvyFormDate")
    if (!Direction.isValidCode(aspect)) problemFields.append("rwAvyFormAspectAC")
    if (!isValidSlopeAngle(angle)) problemFields.append("rwAvyFormAngle")

    if (problemFields.isEmpty) {
      saveReport()
    } else {
      val problemFieldJsonArray = Printer.compact(
        JsonAST.render(JArray(problemFields.toList map(field => JString(field)))))
      JsRaw(s"avyEyesView.currentReport.highlightErrorFields($problemFieldJsonArray)").cmd &
      errorDialog("rwAvyFormValidationError")
    }
  }

  def saveReport(): JsCmd = {
    val avalancheFromValues = createAvalancheFromValues
    val jsDialogCmd = if (!user.isAuthorizedToEditAvalanche(extId, S.param(EditParam))) {
      logger.error(s"Unauthorized to save avalanche $extId")
      errorDialog("avyReportSaveError")
    } else try {
      dal.getAvalanche(extId) match {
        case Some(existingAvalanche) =>
          if (user.isAuthorizedToEditAvalanche(existingAvalanche, S.param(EditParam))) {
            val updatedAvalanche = avalancheFromValues.copy(createTime = existingAvalanche.createTime)
            dal.updateAvalanche(updatedAvalanche)
            logger.info(s"Avalanche $extId successfully updated")

            if (updatedAvalanche.viewable) {
              s3.allowPublicImageAccess(updatedAvalanche.extId)
            } else {
              s3.denyPublicImageAccess(updatedAvalanche.extId)
            }

            infoDialog("avyReportUpdateSuccess")
          } else {
            logger.error(s"User not authorized to update avalanche $extId")
            errorDialog("avyReportSaveError")
          }
        case None =>
          val newAvalanche = avalancheFromValues.copy(viewable = true)
          dal.insertAvalanche(newAvalanche)
          logger.info(s"Avalanche $extId successfully inserted")
          s3.allowPublicImageAccess(newAvalanche.extId)
          sendSubmissionNotifications(newAvalanche, submitterEmail)
          val avalancheUrl = R.avalancheUrl(newAvalanche.extId)
          infoDialog("avyReportInsertSuccess", avalancheUrl, avalancheUrl)

      }
    } catch {
      case e: Exception => {
        logger.error(s"Error saving avalanche $extId", e)
        errorDialog("avyReportSaveError")
      }
    } finally {
      unreserveExtId(extId)
    }

    jsDialogCmd & Call("avyEyesView.resetView").cmd
  }

  def deleteReport(extIdToDelete: String) = user.isAdminSession match {
    case false =>
      logger.error(s"Unauthorized to delete avalanche $extId")
      errorDialog("avyReportDeleteError")
    case true => try {
      dal.deleteAvalanche(extIdToDelete)
      s3.deleteAllImages(extIdToDelete)
      logger.info(s"Avalanche $extIdToDelete deleted")
      infoDialog("avyReportDeleteSuccess")
    } catch {
      case e: Exception => {
        logger.error(s"Error deleting avalanche $extIdToDelete", e)
        errorDialog("avyReportDeleteError")
      }
    }
  }

  private def createAvalancheFromValues = Avalanche(
    createTime = DateTime.now,
    updateTime = DateTime.now,
    extId = extId,
    viewable = viewable,
    submitterEmail = submitterEmail,
    submitterExp = ExperienceLevel.fromCode(submitterExp),
    location = Coordinate(strToDblOrZero(lng), strToDblOrZero(lat), strToDblOrZero(elevation)),
    areaName = areaName,
    date = strToDate(dateStr),
    slope = Slope(
      aspect = Direction.fromCode(aspect),
      angle = strToIntOrNegOne(angle),
      elevation = strToIntOrNegOne(elevation)
    ),
    weather = Weather(
      recentSnow = strToIntOrNegOne(recentSnow),
      recentWindSpeed = WindSpeed.fromCode(recentWindSpeed),
      recentWindDirection = Direction.fromCode(recentWindDirection)
    ),
    classification = Classification(
      avyType = AvalancheType.fromCode(avyType),
      trigger = AvalancheTrigger.fromCode(avyTrigger),
      triggerModifier = AvalancheTriggerModifier.fromCode(avyTriggerModifier),
      interface = AvalancheInterface.fromCode(avyInterface),
      rSize = strToDblOrZero(rSize),
      dSize = strToDblOrZero(dSize)
    ),
    humanNumbers = HumanNumbers(
      modeOfTravel = ModeOfTravel.fromCode(modeOfTravel),
      caught = strToIntOrNegOne(caught),
      partiallyBuried = strToIntOrNegOne(partiallyBuried),
      fullyBuried = strToIntOrNegOne(fullyBuried),
      injured = strToIntOrNegOne(injured),
      killed = strToIntOrNegOne(killed)
    ),
    comments = if (!comments.isEmpty) Some(escapeJava(comments)) else None,
    perimeter = if (!coordStr.isEmpty) coordStr.trim.split(" ").toSeq.map(Coordinate.fromString) else Seq.empty
  )

  private def sendSubmissionNotifications(a: Avalanche, submitterEmail: String) = {
    configureMailer()

    val adminBody = R.localizedStringAsXml("msg.avyReportSubmitEmailAdminBody", submitterEmail, a.extId, a.title, R.avalancheUrl(a.extId))

    sendMail(adminEmailFrom, Subject(R.localizedString("msg.avyReportSubmitEmailAdminSubject", submitterEmail)),
      XHTMLMailBodyType(adminBody) :: To(adminEmailFrom.address) :: Nil : _*)

    val submitterBody = R.localizedStringAsXml("msg.avyReportSubmitEmailSubmitterBody", a.title, R.avalancheUrl(a.extId), R.avalancheEditUrl(a))
    sendMail(adminEmailFrom, Subject(R.localizedString("msg.avyReportSubmitEmailSubmitterSubject", a.areaName)),
      XHTMLMailBodyType(submitterBody) :: To(submitterEmail) :: Nil : _*)
  }

  private[snippet] def configureMailer() = {
    customProperties = Map (
      "mail.smtp.host" -> R.getProperty("mail.smtp.host"),
      "mail.smtp.port" -> R.getProperty("mail.smtp.port"),
      "mail.smtp.auth" -> R.getProperty("mail.smtp.auth"),
      "mail.smtp.starttls.enable" -> R.getProperty("mail.smtp.starttls.enable")
    )

    if (R.getBooleanProperty("mail.smtp.auth")) {
      authenticator = Full(new Authenticator() {
        override def getPasswordAuthentication = new PasswordAuthentication(
          R.getProperty("mail.admin.address"),
          R.getProperty("mail.admin.pw")
        )
      })
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
