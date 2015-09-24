package com.avyeyes.util

import com.avyeyes.service.{ResourceService, Injectors}
import org.joda.time.DateTime
import org.mockito.Matchers
import org.specs2.execute.{Result, AsResult}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.AroundExample

import scala.xml.Unparsed

class ConvertersTest extends Specification {

  "Date parsing, formatting, and validation" >> {
    "Parse a string to a date" >> {
      val dt = Converters.strToDate("08-26-2014")

      dt.dayOfMonth.get must_== 26
      dt.monthOfYear.get must_== 8
      dt.year.get must_== 2014
    }

    "Format date to a string" >> {
      val dt = new DateTime(2014, 9, 8, 0, 0)
      Converters.dateToStr(dt) must_== "09-08-2014"
    }
  }

  "String to Double conversion" >> {
    "Work on doubles of different precisions" >> {
      Converters.strToDblOrZero("23") must_== 23
      Converters.strToDblOrZero("23.0") must_== 23
    }

    "Return 0 for unparsable strings" >> {
      Converters.strToDblOrZero("blah") must_== 0
    }
  }

  "Avalanche size Double to String conversion" >> {
    "Work on sizes of different precesions" >> {
      Converters.sizeToStr(3) must_== "3.0"
      Converters.sizeToStr(3.5) must_== "3.5"
    }
  }

  "Avalanche size Double to String conversion" >> {
    "Work on sizes of different precesions" >> {
      Converters.sizeToStr(3) must_== "3.0"
      Converters.sizeToStr(3.5) must_== "3.5"
    }
  }

}