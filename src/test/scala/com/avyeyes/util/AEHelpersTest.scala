package com.avyeyes.util

import java.util.Calendar

import bootstrap.liftweb.Boot
import com.avyeyes.model.enums.{AvalancheTrigger, AvalancheType, Sky}
import com.avyeyes.test._
import com.avyeyes.util.AEConstants._
import net.liftweb.http.S
import org.apache.commons.lang3.RandomStringUtils

class AEHelpersTest extends WebSpec2(Boot().boot _) {
  "Date parsing, formatting, and validation" should {
    "Parse a string to a date" withSFor("/") in {
      val cal: Calendar = Calendar.getInstance
      cal.setTime(AEHelpers.strToDate("08-26-2014"))

      cal.get(Calendar.DAY_OF_MONTH) must_== 26
      cal.get(Calendar.MONTH) must_== Calendar.AUGUST
      cal.get(Calendar.YEAR) must_== 2014
    }

    "Format date to a string" withSFor("/") in {
      val cal: Calendar = Calendar.getInstance
      cal.set(Calendar.YEAR, 2014)
      cal.set(Calendar.MONTH, Calendar.SEPTEMBER)
      cal.set(Calendar.DAY_OF_MONTH, 8)

      AEHelpers.dateToStr(cal.getTime) must_== "09-08-2014"
    }

    "Validate date strings" withSFor("/") in {
      AEHelpers.isValidDate("") must beFalse
      AEHelpers.isValidDate("asdf") must beFalse
      AEHelpers.isValidDate("09082014") must beFalse
      AEHelpers.isValidDate("03-23-2014") must beTrue
    }
  }

  "Message retrieval" should {
    "Parse messages as xml" withSFor("/") in {
      val xml = AEHelpers.getMessage("loadingText")
      xml must beAnInstanceOf[scala.xml.Unparsed]
      xml.text must contain("<p>")
    }

    "Insert params in messages" withSFor("/") in {
      val xml = AEHelpers.getMessage("browserNotSupported", ChromeVersion,
          FirefoxVersion, OperaVersion, SafariVersion, IeVersion)
      xml.text must contain(ChromeVersion.toString)
      xml.text must contain(FirefoxVersion.toString)
      xml.text must contain(OperaVersion.toString)
      xml.text must contain(SafariVersion.toString)
      xml.text must contain(IeVersion.toString)
    }
  }

  "String to Double conversion" should {
    "Work on doubles of different precisions" withSFor("/") in {
      AEHelpers.strToDblOrZero("23") must_== 23
      AEHelpers.strToDblOrZero("23.0") must_== 23
    }

    "Return 0 for unparsable strings" withSFor("/") in {
      AEHelpers.strToDblOrZero("blah") must_== 0
    }
  }

  "Avalanche size Double to String conversion" should {
    "Work on sizes of different precesions" withSFor("/") in {
      AEHelpers.sizeToStr(3) must_== "3.0"
      AEHelpers.sizeToStr(3.5) must_== "3.5"
    }

    "Return localized 'Unknown' for size 0" withSFor("/") in {
      AEHelpers.sizeToStr(0) must_== S.?("enum.U")
    }
  }

  "Avalanche size Double to String conversion" should {
    "Work on sizes of different precesions" withSFor("/") in {
      AEHelpers.sizeToStr(3) must_== "3.0"
      AEHelpers.sizeToStr(3.5) must_== "3.5"
    }

    "Return localized 'Unknown' for size 0" withSFor("/") in {
      AEHelpers.sizeToStr(0) must_== S.?("enum.U")
    }
  }

  "Valid External ID check" should {
    "Return false for None" withSFor("/") in {
      AEHelpers.isValidExtId(None) must beFalse
    }

    "Return false if ID length is wrong" withSFor("/") in {
      val shortExtId = RandomStringUtils.random(ExtIdLength-1, ExtIdChars)
      val longExtId = RandomStringUtils.random(ExtIdLength+1, ExtIdChars)
      AEHelpers.isValidExtId(Some(shortExtId)) must beFalse
      AEHelpers.isValidExtId(Some(longExtId)) must beFalse
    }

    "Return false if ID contains a bad char" withSFor("/") in {
      val goodExtId = RandomStringUtils.random(ExtIdLength, ExtIdChars)
      AEHelpers.isValidExtId(Some(goodExtId.replace(goodExtId.charAt(4), '~'))) must beFalse
    }

    "Return true if ID is valid" withSFor("/") in {
      val goodExtId = RandomStringUtils.random(ExtIdLength, ExtIdChars)
      AEHelpers.isValidExtId(Some(goodExtId)) must beTrue
    }
  }

  "Bad word check" should {
    "Catch bad words in a string" withSFor("/") in {
      AEHelpers.containsBadWord("what a fucking day!") must beTrue
      AEHelpers.containsBadWord("what a lovely day!") must beFalse
    }

    "Catch bad words in external IDs" withSFor("/") in {
      AEHelpers.containsBadWord("193tit3k") must beTrue
      AEHelpers.containsBadWord("49fk9d3k") must beFalse
    }
  }

  "Enum utilities" should {
    "Recognize valid and invalid enum values" withSFor("/") in {
      AEHelpers.isValidEnumValue(AvalancheType, "") must beFalse
      AEHelpers.isValidEnumValue(AvalancheType, "YX") must beFalse
      AEHelpers.isValidEnumValue(AvalancheType, "HS") must beTrue
    }

    "Return an enum value given the enum name" withSFor("/") in {
      AEHelpers.enumWithNameOr(AvalancheTrigger, "", AvalancheTrigger.U) must_== AvalancheTrigger.U
      AEHelpers.enumWithNameOr(AvalancheTrigger, "ZZ", AvalancheTrigger.U) must_== AvalancheTrigger.U
      AEHelpers.enumWithNameOr(AvalancheTrigger, "AS", AvalancheTrigger.U) must_== AvalancheTrigger.AS
    }
  }

  "Email validation" should {
    "Recognize valid and invalid email addresses" withSFor("/") in {
      AEHelpers.isValidEmail("") must beFalse
      AEHelpers.isValidEmail("joebob") must beFalse
      AEHelpers.isValidEmail("joebob@") must beFalse
      AEHelpers.isValidEmail("joebob@company.com") must beTrue
    }
  }

  "Slope angle validation" should {
    "Recognize valid and invalid slope angles" withSFor("/") in {
      AEHelpers.isValidSlopeAngle("") must beFalse
      AEHelpers.isValidSlopeAngle("0") must beFalse
      AEHelpers.isValidSlopeAngle("90") must beFalse
      AEHelpers.isValidSlopeAngle("-5") must beFalse
      AEHelpers.isValidSlopeAngle("25") must beTrue
    }
  }
}