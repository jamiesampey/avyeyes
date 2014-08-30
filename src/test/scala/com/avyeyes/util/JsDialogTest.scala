package com.avyeyes.util

import com.avyeyes.test.AvyEyesSpec
import net.liftweb.http.S

class JsDialogTest extends AvyEyesSpec {
    "Info dialog" should {
      "Use the correct dialog title" withSFor("/") in {
        val cmd = JsDialog.info("avySearchSuccess").toJsCmd
        cmd must startWith("avyeyes.showModalDialog")
        cmd must contain(S.?("title.infoDialog"))
      }
      
      "Pass params down to message" withSFor("/") in {
        val cmd = JsDialog.info("avySearchSuccess", 5).toJsCmd
        cmd must contain("5")
      }
    }
    
    "Error dialog" should {
      "Use the correct dialog title" withSFor("/") in {
        val cmd = JsDialog.error("eyeTooHigh").toJsCmd
        cmd must startWith("avyeyes.showModalDialog")
        cmd must contain(S.?("title.errorDialog"))
      }
      
      "Select the correct message" withSFor("/") in {
        val cmd = JsDialog.error("eyeTooHigh").toJsCmd
        cmd must contain(S.?("msg.eyeTooHigh"))
      }
    }
}