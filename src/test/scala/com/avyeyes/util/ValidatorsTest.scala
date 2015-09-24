package com.avyeyes.util

import com.avyeyes.util.Constants._
import org.apache.commons.lang3.RandomStringUtils
import org.specs2.mutable.Specification

class ValidatorsTest extends Specification {
  "Date String validation" >> {
    Validators.isValidDate("") must beFalse
    Validators.isValidDate("asdf") must beFalse
    Validators.isValidDate("09082014") must beFalse
    Validators.isValidDate("03-23-2014") must beTrue
  }

  "Email validation" >> {
    Validators.isValidEmail("") must beFalse
    Validators.isValidEmail("joebob") must beFalse
    Validators.isValidEmail("joebob@") must beFalse
    Validators.isValidEmail("joebob@company.com") must beTrue
  }

  "Slope angle validation" >> {
    Validators.isValidSlopeAngle("") must beFalse
    Validators.isValidSlopeAngle("0") must beFalse
    Validators.isValidSlopeAngle("90") must beFalse
    Validators.isValidSlopeAngle("-5") must beFalse
    Validators.isValidSlopeAngle("25") must beTrue
  }

  "External ID validation" >> {
    "Return false for None" >> {
      Validators.isValidExtId(None) must beFalse
    }

    "Return false if ID length is wrong" >> {
      val shortExtId = RandomStringUtils.random(ExtIdLength-1, ExtIdChars)
      val longExtId = RandomStringUtils.random(ExtIdLength+1, ExtIdChars)
      Validators.isValidExtId(Some(shortExtId)) must beFalse
      Validators.isValidExtId(Some(longExtId)) must beFalse
    }

    "Return false if ID contains a bad char" >> {
      val goodExtId = RandomStringUtils.random(ExtIdLength, ExtIdChars)
      Validators.isValidExtId(Some(goodExtId.replace(goodExtId.charAt(4), '~'))) must beFalse
    }

    "Return true if ID is valid" >> {
      val goodExtId = RandomStringUtils.random(ExtIdLength, ExtIdChars)
      Validators.isValidExtId(Some(goodExtId)) must beTrue
    }
  }
}
