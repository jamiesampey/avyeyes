package com.avyeyes.util

import com.avyeyes.test.WebSpec2

class AEHelpersTest extends WebSpec2 {
    "String to Double conversion" should {
      "Work on doubles of different precisions" withSFor("/") in {
        AEHelpers.strToDbl("23") must_== 23
        AEHelpers.strToDbl("23.0") must_== 23
      }
    }
}

