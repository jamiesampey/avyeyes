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


object AEHelpers {
    private val UnknownLabel = S.?("enum.U")
    
	private val df = new SimpleDateFormat("MM-dd-yyyy")

    def getProp(prop: String) = Props.get(prop) openOr(prop)
    
    def parseDateStr(str: String) = df.parse(str)
	
    def getMessage(id: String, params: Any*) = Unparsed(S.?(s"msg.$id", params:_*))
    
	def strToDbl(str: String): Double = asDouble(str) openOr 0

	def sizeToStr(size: Double): String = if (size == 0) UnknownLabel else size.toString
	
	def strToHumanNumber(str: String): Int = asInt(str) openOr -1
	
	def humanNumberToStr(hn: Int): String = if (hn == -1) UnknownLabel else hn.toString

	def isValidExtId(extId: Option[String]): Boolean = extId match {
	  case None => false
	  case Some(s) if s.length != ExtIdLength => false
	  case Some(s) if s exists (c => !ExtIdChars.contains(c)) => false
	  case _ => true
	}
    
    def getRemoteIP(request: Box[HTTPRequest]) = request.map(_.remoteAddress).openOr(UnknownLabel)
}