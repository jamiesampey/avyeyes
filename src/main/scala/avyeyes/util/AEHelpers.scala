package avyeyes.util

import avyeyes.util.AEConstants._
import java.text.SimpleDateFormat
import java.util.Date

import net.liftweb.util.Helpers._

object AEHelpers {
	// Date helpers
	private val df = new SimpleDateFormat("MM-dd-yyyy")
	def parseDateStr(str: String) = df.parse(str)
	
	// Snippet helpers
	def strToDbl(str: String): Double = asDouble(str) openOr 0
	def sizeToStr(size: Double): String = if (size == 0) UNKNOWN_LABEL else size.toString
	def strToHumanNumber(str: String): Int = asInt(str) openOr -1
	def humanNumberToStr(hn: Int): String = if (hn == -1) UNKNOWN_LABEL else hn.toString
}