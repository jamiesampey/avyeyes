package com.jamiesampey.avyeyes.util

import org.joda.time.DateTime
import org.specs2.mutable.Specification

class ConvertersTest extends Specification {

  "Date parsing, formatting, and validation" >> {
    "Parse a string to a date" >> {
      val dt = Converters.strToDate("2014-08-26")

      dt.dayOfMonth.get must_== 26
      dt.monthOfYear.get must_== 8
      dt.year.get must_== 2014
    }

    "Format date to a string" >> {
      val dt = new DateTime(2014, 9, 8, 0, 0)
      Converters.dateToStr(dt) must_== "2014-09-08"
    }
  }
}