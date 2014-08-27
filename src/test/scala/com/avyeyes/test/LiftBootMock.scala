package com.avyeyes.test

import net.liftweb.http.LiftRules

object LiftBootMock {
  def boot() = {
    LiftRules.resourceNames = "text" :: "enum" :: "help" :: Nil
  }
}
