package com.avyeyes.util

import javax.mail.internet.{AddressException, InternetAddress}

import com.avyeyes.util.Constants._
import net.liftweb.common.Box
import net.liftweb.http.provider.HTTPRequest
import net.liftweb.http.{LiftRules, S}
import net.liftweb.util.Helpers.{asDouble, asInt}
import net.liftweb.util.Props
import org.apache.commons.lang3.Validate
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.xml.{NodeSeq, Unparsed}


object Helpers {
  val dtf = DateTimeFormat.forPattern("MM-dd-yyyy")
  
  val UnknownEnumCode = "enum.U"
  
  def getProp(prop: String) = Props.get(prop) openOr(prop)
    
  def strToDate(str: String): DateTime = dtf.parseDateTime(str)
	
  def dateToStr(dt: DateTime): String = dt.toString(dtf)
  
  def getMessage(id: String, params: Any*) = Unparsed(S.?(s"msg.$id", params:_*))
    
	def strToDblOrZero(str: String): Double = asDouble(str) openOr 0

	def strToIntOrNegOne(str: String): Int = asInt(str) openOr -1

	def sizeToStr(size: Double): String = if (size == 0) S.?(UnknownEnumCode) else size.toString
	 
	def isValidExtId(extId: Option[String]): Boolean = extId match {
	  case None => false
	  case Some(s) if s.length != ExtIdLength => false
	  case Some(s) if s exists (c => !ExtIdChars.contains(c)) => false
	  case _ => true
	}

  def isValidEnumValue(enum: Enumeration, code: String): Boolean = {
    !code.isEmpty && enum.values.exists(_.toString.endsWith(code))
  }

  def isValidEmail(email: String): Boolean = {
    val isValid = try {
      new InternetAddress(email).validate()
      true
    } catch {
      case e: AddressException => false
    }
    isValid
  }

  def isValidSlopeAngle(angle: String): Boolean = {
    val isValid = try {
      Validate.exclusiveBetween(0, 90, angle.toInt)
      true
    } catch {
      case iae: IllegalArgumentException => false
    }
    isValid
  }

  def isValidDate(dateStr: String): Boolean = {
    val isValid = try {
      strToDate(dateStr)
      true
    } catch {
      case iae: IllegalArgumentException => false
    }
    isValid
  }

  def getRemoteIP(request: Box[HTTPRequest]) = request.map(_.remoteAddress).openOr(S.?(UnknownEnumCode))
  
  def getHttpBaseUrl = {
    val httpPort = getProp("httpPort")
    s"http://${getProp("hostname")}${if (httpPort != 80) s":$httpPort" else ""}/"
  }
  
  def getHttpsBaseUrl = {
    val httpsPort = getProp("httpsPort")
    s"https://${getProp("hostname")}${if (httpsPort != 443) s":$httpsPort" else ""}/"
  }
  
  lazy val BadWords = {
    val badWordsXml = LiftRules.loadResourceAsXml("/badWords.xml") openOr NodeSeq.Empty
    (badWordsXml \\ "word").toList.map(node => node.text)
  } 
    
  def containsBadWord(str: String): Boolean = {
    BadWords.find(w => str.contains(w)) match {
      case Some(badWord) => true
      case None => false
    }
  }
}