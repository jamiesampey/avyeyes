package com.avyeyes.util

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.util.{Failure, Success, Try}

object Converters {
  private val dtf = DateTimeFormat.forPattern("MM-dd-yyyy")
  
  def strToDate(str: String): DateTime = dtf.parseDateTime(str)
	
  def dateToStr(dt: DateTime): String = dt.toString(dtf)
}