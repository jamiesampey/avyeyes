package com.avyeyes.util

import javax.mail.internet.InternetAddress

import com.avyeyes.util.Converters._
import com.avyeyes.util.Constants._
import org.apache.commons.lang3.Validate

import scala.util.{Failure, Success, Try}

object Validators {

  def isValidExtId(extId: Option[String]): Boolean = extId match {
    case None => false
    case Some(s) if s.length != ExtIdLength => false
    case Some(s) if s exists (c => !ExtIdChars.contains(c)) => false
    case _ => true
  }

  def isValidEmail(email: String): Boolean = {
    Try(new InternetAddress(email).validate()) match {
      case Success(addr) => true
      case Failure(ex) => false
    }
  }
  
  def isValidSlopeAngle(angle: String): Boolean = {
    Try(Validate.exclusiveBetween(0, 90, angle.toInt)) match {
      case Success(unit) => true
      case Failure(ex) => false
    }
  }

  def isValidDate(dateStr: String): Boolean = {
    Try(strToDate(dateStr)) match {
      case Success(dt) => true
      case Failure(ex) => false
    }
  }
}
