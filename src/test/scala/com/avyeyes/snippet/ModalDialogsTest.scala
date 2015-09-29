package com.avyeyes.snippet

import com.avyeyes.service.ResourceService
import org.mockito.Matchers._
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class ModalDialogsTest extends Specification with Mockito {

  class ModalDialogsForTest extends ModalDialogs {
    val R = mock[ResourceService]
  }
  
  "Info dialog" >> {
    "Retrieve the correct localized title and message strings" >> {
      val modalDialogs = new ModalDialogsForTest
      val msgIdCapture = capture[String]

      val cmd = modalDialogs.infoDialog("avySearchSuccess").toJsCmd
      there was two(modalDialogs.R).localizedString(msgIdCapture, anyVararg())

      cmd must startWith("avyEyesView.showModalDialog")
      msgIdCapture.values.get(0) mustEqual "title.infoDialog"
      msgIdCapture.values.get(1) mustEqual "msg.avySearchSuccess"
    }
  }

  "Error dialog" >> {
    "Retrieve the correct localized title and message strings" >> {
      val modalDialogs = new ModalDialogsForTest
      val msgIdCapture = capture[String]

      val cmd = modalDialogs.errorDialog("eyeTooHigh").toJsCmd
      there was two(modalDialogs.R).localizedString(msgIdCapture, anyVararg())

      cmd must startWith("avyEyesView.showModalDialog")
      msgIdCapture.values.get(0) mustEqual "title.errorDialog"
      msgIdCapture.values.get(1) mustEqual "msg.eyeTooHigh"
    }
  }
}