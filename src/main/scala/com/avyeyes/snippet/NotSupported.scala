package com.avyeyes.snippet

import net.liftweb.util.Helpers._
import com.avyeyes.util.Helpers._
import com.avyeyes.util.Constants._

class NotSupported {
    def render = {
      "#browserNotSupportedMsg" #> <span id="browserNotSupportedMsg">{getMessage("browserNotSupported",
        ChromeMinVersion, FirefoxMinVersion, SafariMinVersion, IeMinVersion)}</span>
    }
}