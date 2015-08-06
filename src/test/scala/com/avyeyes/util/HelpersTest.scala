package com.avyeyes.util

import bootstrap.liftweb.Boot
import com.avyeyes.model.enums.{AvalancheTrigger, AvalancheType}
import com.avyeyes.test._
import com.avyeyes.util.Constants._
import net.liftweb.http.S
import org.apache.commons.lang3.RandomStringUtils
import org.joda.time.DateTime

class HelpersTest extends WebSpec2(Boot().boot _) {
  "Date parsing, formatting, and validation" should {
    "Parse a string to a date" withSFor "/" in {
      val dt = Helpers.strToDate("08-26-2014")

      dt.dayOfMonth.get must_== 26
      dt.monthOfYear.get must_== 8
      dt.year.get must_== 2014
    }

    "Format date to a string" withSFor "/" in {
      val dt = new DateTime(2014, 9, 8, 0, 0)
      Helpers.dateToStr(dt) must_== "09-08-2014"
    }

    "Validate date strings" withSFor "/"  in {
      Helpers.isValidDate("") must beFalse
      Helpers.isValidDate("asdf") must beFalse
      Helpers.isValidDate("09082014") must beFalse
      Helpers.isValidDate("03-23-2014") must beTrue
    }
  }

  "Message retrieval" should {
    "Parse messages as xml" withSFor "/" in {
      val xml = Helpers.getMessage("loadingText")
      xml must beAnInstanceOf[scala.xml.Unparsed]
      xml.text must contain("<p>")
    }

    "Insert params in messages" withSFor "/" in {
      val xml = Helpers.getMessage("browserNotSupported", FirefoxMinVersion,
        OperaMinVersion, IeMinVersion, ChromeMinVersion)
      xml.text must contain(ChromeMinVersion.toString)
      xml.text must contain(FirefoxMinVersion.toString)
      xml.text must contain(OperaMinVersion.toString)
      xml.text must contain(IeMinVersion.toString)
    }
  }

  "String to Double conversion" should {
    "Work on doubles of different precisions" withSFor "/" in {
      Helpers.strToDblOrZero("23") must_== 23
      Helpers.strToDblOrZero("23.0") must_== 23
    }

    "Return 0 for unparsable strings" withSFor "/" in {
      Helpers.strToDblOrZero("blah") must_== 0
    }
  }

  "Avalanche size Double to String conversion" should {
    "Work on sizes of different precesions" withSFor "/" in {
      Helpers.sizeToStr(3) must_== "3.0"
      Helpers.sizeToStr(3.5) must_== "3.5"
    }

    "Return localized 'Unknown' for size 0" withSFor "/" in {
      Helpers.sizeToStr(0) must_== S.?("enum.U")
    }
  }

  "Avalanche size Double to String conversion" should {
    "Work on sizes of different precesions" withSFor "/" in {
      Helpers.sizeToStr(3) must_== "3.0"
      Helpers.sizeToStr(3.5) must_== "3.5"
    }

    "Return localized 'Unknown' for size 0" withSFor "/" in {
      Helpers.sizeToStr(0) must_== S.?("enum.U")
    }
  }

  "Valid External ID check" should {
    "Return false for None" withSFor "/" in {
      Helpers.isValidExtId(None) must beFalse
    }

    "Return false if ID length is wrong" withSFor "/" in {
      val shortExtId = RandomStringUtils.random(ExtIdLength-1, ExtIdChars)
      val longExtId = RandomStringUtils.random(ExtIdLength+1, ExtIdChars)
      Helpers.isValidExtId(Some(shortExtId)) must beFalse
      Helpers.isValidExtId(Some(longExtId)) must beFalse
    }

    "Return false if ID contains a bad char" withSFor "/" in {
      val goodExtId = RandomStringUtils.random(ExtIdLength, ExtIdChars)
      Helpers.isValidExtId(Some(goodExtId.replace(goodExtId.charAt(4), '~'))) must beFalse
    }

    "Return true if ID is valid" withSFor "/" in {
      val goodExtId = RandomStringUtils.random(ExtIdLength, ExtIdChars)
      Helpers.isValidExtId(Some(goodExtId)) must beTrue
    }
  }

  "Bad word check" should {
    "Catch bad words in a string" withSFor "/" in {
      Helpers.containsBadWord("what a fucking day!") must beTrue
      Helpers.containsBadWord("what a lovely day!") must beFalse
    }

    "Catch bad words in external IDs" withSFor "/"  in {
      Helpers.containsBadWord("193tit3k") must beTrue
      Helpers.containsBadWord("49fk9d3k") must beFalse
    }
  }

  "Enum utilities" should {
    "Recognize valid and invalid enum values" withSFor "/" in {
      Helpers.isValidEnumValue(AvalancheType, "") must beFalse
      Helpers.isValidEnumValue(AvalancheType, "YX") must beFalse
      Helpers.isValidEnumValue(AvalancheType, "HS") must beTrue
    }
  }

  "Email validation" should {
    "Recognize valid and invalid email addresses" withSFor "/" in {
      Helpers.isValidEmail("") must beFalse
      Helpers.isValidEmail("joebob") must beFalse
      Helpers.isValidEmail("joebob@") must beFalse
      Helpers.isValidEmail("joebob@company.com") must beTrue
    }
  }

  "Slope angle validation" should {
    "Recognize valid and invalid slope angles" withSFor "/" in {
      Helpers.isValidSlopeAngle("") must beFalse
      Helpers.isValidSlopeAngle("0") must beFalse
      Helpers.isValidSlopeAngle("90") must beFalse
      Helpers.isValidSlopeAngle("-5") must beFalse
      Helpers.isValidSlopeAngle("25") must beTrue
    }
  }
}