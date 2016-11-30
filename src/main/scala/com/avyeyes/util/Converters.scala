package com.avyeyes.util

import net.liftweb.util.Helpers.{asDouble, asInt}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat


object Converters {
  private val dtf = DateTimeFormat.forPattern("MM-dd-yyyy")
  
  def strToDate(str: String): DateTime = dtf.parseDateTime(str)
	
  def dateToStr(dt: DateTime): String = dt.toString(dtf)

	def strToDblOrZero(str: String): Double = asDouble(str) openOr 0

	def strToIntOrNegOne(str: String): Int = asInt(str) openOr -1
}