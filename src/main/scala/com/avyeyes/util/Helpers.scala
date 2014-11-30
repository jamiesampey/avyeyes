package com.avyeyes.util

import java.text.{ParseException, SimpleDateFormat}
import javax.mail.internet.{AddressException, InternetAddress}
import org.apache.commons.lang3.Validate

import scala.xml.Unparsed
import com.avyeyes.util.Constants._
import net.liftweb.common.Box
import net.liftweb.http.S
import net.liftweb.http.provider.HTTPRequest
import net.liftweb.util.Helpers.asDouble
import net.liftweb.util.Helpers.asInt
import net.liftweb.util.Props
import java.util.Date
import net.liftweb.http.LiftRules
import scala.xml.NodeSeq


object Helpers {
  val DatePattern = "MM-dd-yyyy"
  
  val UnknownEnumCode = "enum.U"
  
  def getProp(prop: String) = Props.get(prop) openOr(prop)
    
  def strToDate(str: String): Date = new SimpleDateFormat(DatePattern).parse(str)
	
  def dateToStr(d: Date): String = new SimpleDateFormat(DatePattern).format(d)
  
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

  def isValidEnumValue(enum: Enumeration, name: String): Boolean = {
    val enumOpt: Option[Enumeration#Value] = try {
      Some(enum.withName(name))
    } catch {
      case e: NoSuchElementException => None
    }
    enumOpt.isDefined
  }

  def enumWithNameOr[T <: Enumeration](enum: T, name: String, defaultValue: T#Value): T#Value = {
    val enumOpt: Option[T#Value] = try {
      Some(enum.withName(name))
    } catch {
      case e: NoSuchElementException => None
    }
    enumOpt getOrElse defaultValue
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
      case e: IllegalArgumentException => false
    }
    isValid
  }

  def isValidDate(dateStr: String): Boolean = {
    val isValid = try {
      strToDate(dateStr)
      true
    } catch {
      case pe: ParseException => false
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