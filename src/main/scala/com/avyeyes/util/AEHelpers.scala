package com.avyeyes.util

import com.avyeyes.util.AEConstants._
import com.avyeyes.model.enums.Aspect
import com.avyeyes.model.enums._
import java.text.SimpleDateFormat
import net.liftweb.util.Helpers._
import net.liftweb.json.JsonAST._
import net.liftweb.util.Props
import scala.xml.Unparsed
import net.liftweb.http.S


object AEHelpers {
    private val UNKNOWN_LABEL = S.?("enum.U")
	private val df = new SimpleDateFormat("MM-dd-yyyy")

    def parseDateStr(str: String) = df.parse(str)
	
    def getMessage(id: String, params: Any*) = Unparsed(S.?(s"msg.$id", params:_*))
    
	// Snippet helpers
	def strToDbl(str: String): Double = asDouble(str) openOr 0
	def sizeToStr(size: Double): String = if (size == 0) UNKNOWN_LABEL else size.toString
	def strToHumanNumber(str: String): Int = asInt(str) openOr -1
	def humanNumberToStr(hn: Int): String = if (hn == -1) UNKNOWN_LABEL else hn.toString

	def isValidExtId(extId: Option[String]): Boolean = extId match {
	  case None => false
	  case Some(s) if s.length != EXT_ID_LENGTH => false
	  case Some(s) if s exists (c => !EXT_ID_CHARS.contains(c)) => false
	  case _ => true
	}
}