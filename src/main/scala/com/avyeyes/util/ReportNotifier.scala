package com.avyeyes.util

import javax.mail._

import com.avyeyes.model.Avalanche
import com.avyeyes.util.Helpers._
import net.liftweb.common._
import net.liftweb.util.Mailer._
import net.liftweb.util._

object ReportNotifier extends Mailer with Loggable {
  val isAuth = Props.get("mail.smtp.auth", "false").toBoolean
  val avyeyesAdminAddressFrom = "Avy Eyes"
  val avyeyesAdminAddress = getProp("mail.admin.address")

  customProperties = Map (
    "mail.smtp.host" -> getProp("mail.smtp.host"),
    "mail.smtp.port" -> getProp("mail.smtp.port"),
    "mail.smtp.auth" -> getProp("mail.smtp.auth"),
    "mail.smtp.starttls.enable" -> getProp("mail.smtp.starttls.enable")
  )

  if (isAuth) {
    (Props.get("mail.admin.address"), Props.get("mail.admin.pw")) match {
      case (Full(username), Full(password)) =>
        authenticator = Full(new Authenticator() {
          override def getPasswordAuthentication = new PasswordAuthentication(username, password)
        })
      case _ => logger.error("Missing username and/or password for SMTP email")
    }
  }

  def sendSubmissionNotifications(a: Avalanche, submitterEmail: String) = {
    val adminBody = getMessage("avyReportSubmitEmailAdminBody", submitterEmail, a.extId, a.getTitle, a.getExtUrl)
    sendMail(From(avyeyesAdminAddress, Full(avyeyesAdminAddressFrom)),
      Subject(getMessage("avyReportSubmitEmailAdminSubject", submitterEmail).toString),
      (XHTMLMailBodyType(adminBody) :: To(avyeyesAdminAddress) :: Nil) : _*)

    val submitterBody = getMessage("avyReportSubmitEmailSubmitterBody", a.extId, a.getExtUrl).toString
    sendMail(From(avyeyesAdminAddress, Full(avyeyesAdminAddressFrom)),
      Subject(getMessage("avyReportSubmitEmailSubmitterSubject", a.extId).toString),
      (PlainMailBodyType(submitterBody) :: To(submitterEmail) :: Nil) : _*)
  }

  def sendApprovalNotification(a: Avalanche, submitterEmail: String) = {
    val submitterBody = getMessage("avyReportApproveEmailSubmitterBody", a.getTitle, a.getExtUrl).toString
    sendMail(From(avyeyesAdminAddress, Full(avyeyesAdminAddressFrom)),
      Subject(getMessage("avyReportApproveEmailSubmitterSubject", a.extId).toString),
      (PlainMailBodyType(submitterBody) :: To(submitterEmail) :: Nil) : _*)
  }
}
