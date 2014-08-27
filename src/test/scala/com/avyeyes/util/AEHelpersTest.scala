package com.avyeyes.util

import com.avyeyes.test.WebSpec2
import net.liftweb.http.LiftRules
import net.liftweb.http.S
import java.util.Calendar
import scala.xml.NodeSeq
import com.avyeyes.util.AEConstants._
import org.apache.commons.lang3.RandomStringUtils

object Boot {
  def boot() = {
    LiftRules.resourceNames = "text" :: "enum" :: "help" :: Nil
  }
}

class AEHelpersTest extends WebSpec2(Boot.boot _) {
    "Date parsing" should {
      "Parse a normal date correctly" withSFor("/") in {
          val parsedDate = AEHelpers.parseDateStr("08-26-2014")
          val cal: Calendar = Calendar.getInstance()
          cal.setTime(parsedDate)
          
          cal.get(Calendar.DAY_OF_MONTH) must_== 26
          cal.get(Calendar.MONTH) must_== Calendar.AUGUST
          cal.get(Calendar.YEAR) must_== 2014
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
        AEHelpers.strToDbl("23") must_== 23
        AEHelpers.strToDbl("23.0") must_== 23
      }
      
      "Return 0 for unparsable strings" withSFor("/") in {
        AEHelpers.strToDbl("blah") must_== 0
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
    
    "Human number to String conversion" should {
      "Work on normal integers" withSFor("/") in {
        AEHelpers.humanNumberToStr(4) must_== "4"
      }
      
      "Return localized 'Unknown' for -1" withSFor("/") in {
        AEHelpers.humanNumberToStr(-1) must_== S.?("enum.U")
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
}