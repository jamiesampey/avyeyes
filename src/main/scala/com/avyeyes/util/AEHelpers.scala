package com.avyeyes.util

import java.text.SimpleDateFormat
import scala.xml.Unparsed
import com.avyeyes.util.AEConstants._
import net.liftweb.common.Box
import net.liftweb.http.S
import net.liftweb.http.provider.HTTPRequest
import net.liftweb.util.Helpers.asDouble
import net.liftweb.util.Helpers.asInt
import net.liftweb.util.Props
import java.util.Date


object AEHelpers {
  private val UnknownLabel = S.?("enum.U")
  val DatePattern = "MM-dd-yyyy"
  
  def getProp(prop: String) = Props.get(prop) openOr(prop)
    
  def strToDate(str: String): Date = new SimpleDateFormat(DatePattern).parse(str)
	
  def dateToStr(d: Date): String = new SimpleDateFormat(DatePattern).format(d)
  
  def getMessage(id: String, params: Any*) = Unparsed(S.?(s"msg.$id", params:_*))
    
	def strToDblOrZero(str: String): Double = asDouble(str) openOr 0

	def strToIntOrNegOne(str: String): Int = asInt(str) openOr -1

	def sizeToStr(size: Double): String = if (size == 0) UnknownLabel else size.toString
	 
	def humanNumberToStr(hn: Int): String = if (hn == -1) UnknownLabel else hn.toString

	def isValidExtId(extId: Option[String]): Boolean = extId match {
	  case None => false
	  case Some(s) if s.length != ExtIdLength => false
	  case Some(s) if s exists (c => !ExtIdChars.contains(c)) => false
	  case _ => true
	}
    
  def getRemoteIP(request: Box[HTTPRequest]) = request.map(_.remoteAddress).openOr(UnknownLabel)
}