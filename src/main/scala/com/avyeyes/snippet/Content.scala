package com.avyeyes.snippet

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.http.S
import scala.xml.NodeSeq
import scala.xml.Unparsed
import com.avyeyes.util.AEConstants._
import com.avyeyes.util.AEHelpers._
import net.liftweb.http.Req

class Content {
    def render = {
      if (!browserSupported(S.request)) {
          S.redirectTo("notsupported.html")
      } else {
          "label" #> ((ns:NodeSeq) => 
            setupLabel((ns\"@for").text, asBoolean((ns\"@data-required").text) openOr false)) &
          ".avyHeader" #> ((n:NodeSeq) => setupHeader((n\"@id").text)) &
          ".avyMsg" #> ((n:NodeSeq) => setupMessage((n\"@id").text)) &
          ".avyLink" #> ((n:NodeSeq) => setupLink((n\"@id").text)) &
          ".avyButton [value]" #> ((n:NodeSeq) => getButton((n\"@id").text))
      }
    }

    private def browserSupported(reqBox: Box[Req]): Boolean = reqBox match {
      case isDefined if reqBox.get.chromeVersion.isDefined && reqBox.get.chromeVersion.get >= ChromeSupportedVersion => true
      case isDefined if reqBox.get.firefoxVersion.isDefined && reqBox.get.firefoxVersion.get >= FirefoxSupportedVersion => true
      case isDefined if reqBox.get.safariVersion.isDefined && reqBox.get.safariVersion.get >= SafariSupportedVersion => true
      case isDefined if reqBox.get.ieVersion.isDefined && reqBox.get.ieVersion.get >= IeSupportedVersion => true
      case _ => false
    }
        
    private def setupLabel(id: String, required: Boolean): NodeSeq = {
      <label for={id} data-help={Unparsed(S.?(s"help.$id"))} data-required={required.toString}>
          {S.?(s"label.$id")}:{if (required) Unparsed("<span style='color: red;'>&nbsp;*</span>")}
      </label>
    }

    private def setupHeader(id: String): NodeSeq = {
      <span id={id} class="avyHeader">{S.?(s"header.$id")}</span>
    }
    
    private def setupMessage(id: String): NodeSeq = {
      <span id={id} class="avyMsg">{getMessage(id)}</span>
    }
    
    private def setupLink(id: String): NodeSeq = {
      <a id={id} class="avyLink">{S.?(s"link.$id")}</a>
    }
    
    private def getButton(id: String) = S.?(s"button.$id")
}