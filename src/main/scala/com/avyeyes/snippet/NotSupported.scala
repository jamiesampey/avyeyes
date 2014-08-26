package com.avyeyes.snippet

import net.liftweb.common._
import net.liftweb.util.Helpers._
import com.avyeyes.util.AEHelpers._
import com.avyeyes.util.AEConstants._

class NotSupported {
    def render = {
      "#browserNotSupportedMsg" #> getMessage("browserNotSupported", 
          ChromeVersion, FirefoxVersion, OperaVersion, SafariVersion, IeVersion)
    }
}