package com.avyeyes.util

import com.avyeyes.util.AEConstants._
import com.avyeyes.model.enums.Aspect
import com.avyeyes.model.enums._
import java.text.SimpleDateFormat
import net.liftweb.util.Helpers._
import net.liftweb.json.JsonAST._
import net.liftweb.util.Props


object AEHelpers {
    private val UNKNOWN_LABEL = Props.get("label.unknown").get
	private val df = new SimpleDateFormat("MM-dd-yyyy")

    def parseDateStr(str: String) = df.parse(str)
	
	// Snippet helpers
	def strToDbl(str: String): Double = asDouble(str) openOr 0
	def sizeToStr(size: Double): String = if (size == 0) UNKNOWN_LABEL else size.toString
	def strToHumanNumber(str: String): Int = asInt(str) openOr -1
	def humanNumberToStr(hn: Int): String = if (hn == -1) UNKNOWN_LABEL else hn.toString

	def isValidExtId(extId: Option[String]): Boolean = extId match {
	  case None => false
	  case Some(s) if s.length != EXT_ID_LENGTH => false
	  case Some(s) if (s intersect EXT_ID_CHARS).length != EXT_ID_LENGTH => false
	  case _ => true
	}

	def getLookAtHeadingForAspect(aspect: Aspect.Value): Int = aspect match {
	  case Aspect.N => 180
	  case Aspect.NE => 225
	  case Aspect.E => 270
	  case Aspect.SE => 315
	  case Aspect.S => 0
	  case Aspect.SW => 45
	  case Aspect.W => 90
	  case Aspect.NW => 135
	}
}