package com.avyeyes.util

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.util.{Failure, Success, Try}

object Converters {
  private val dtf = DateTimeFormat.forPattern("MM-dd-yyyy")
  
  def strToDate(str: String): DateTime = dtf.parseDateTime(str)
	
  def dateToStr(dt: DateTime): String = dt.toString(dtf)

	def strToDblOrZero(str: String): Double = Try(str.toDouble) match {
    case Success(double) => double
    case Failure(_) => 0
  }

	def strToIntOrNegOne(str: String): Int = Try(str.toInt) match {
    case Success(int) => int
    case Failure(_) => -1
  }
}