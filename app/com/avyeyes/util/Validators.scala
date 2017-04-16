package com.avyeyes.util

import javax.mail.internet.InternetAddress
import com.avyeyes.util.Converters._
import org.apache.commons.lang3.Validate

import scala.util.{Failure, Success, Try}

object Validators {

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
