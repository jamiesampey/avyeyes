package com.avyeyes.util

import com.avyeyes.service.Injectors
import com.avyeyes.util.Constants.UnknownEnumCode
import net.liftweb.util.Helpers.{asDouble, asInt}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat


object Converters {
  val R = Injectors.resources.vend

  val dtf = DateTimeFormat.forPattern("MM-dd-yyyy")
  
  def strToDate(str: String): DateTime = dtf.parseDateTime(str)
	
  def dateToStr(dt: DateTime): String = dt.toString(dtf)

	def strToDblOrZero(str: String): Double = asDouble(str) openOr 0

	def strToIntOrNegOne(str: String): Int = asInt(str) openOr -1

	def sizeToStr(size: Double): String =
    if (size == 0) R.localizedString(UnknownEnumCode)
    else size.toString
}