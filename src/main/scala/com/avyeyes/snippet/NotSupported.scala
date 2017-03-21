package com.avyeyes.snippet

import com.avyeyes.service.Injectors
import net.liftweb.util.Helpers._
import com.avyeyes.util.Constants._

class NotSupported {
  private val R = Injectors.resources.vend

  def render = {
    "#browserNotSupportedMsg" #> <span id="browserNotSupportedMsg">
      {R.localizedStringAsXml("msg.browserNotSupported", ChromeMinVersion, FirefoxMinVersion, SafariMinVersion, IeMinVersion)}
    </span>
  }
}