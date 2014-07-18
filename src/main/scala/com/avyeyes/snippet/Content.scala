package com.avyeyes.snippet

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.http.S
import scala.xml.NodeSeq
import scala.xml.Unparsed
import com.avyeyes.util.AEHelpers._
import scala.xml.Attribute
import scala.xml.Text

class Content {
    def render = {
      "label" #> ((ns:NodeSeq) => setupLabel((ns\"@for").text, asBoolean((ns\"@data-required").text) openOr false)) &
      ".avyHeader" #> ((n:NodeSeq) => setupHeader((n\"@id").text)) &
      ".avyMsg" #> ((n:NodeSeq) => setupMessage((n\"@id").text)) &
      ".avyMenuItem" #> ((n:NodeSeq) => setupMenuItem((n\"@id").text)) &
      ".avyButton [value]" #> ((n:NodeSeq) => getButton((n\"@id").text)) &
      "#avySearchHowItWorks *" #> S.?("link.avySearchHowItWorks")
    }

    private def setupLabel(id: String, required: Boolean): NodeSeq = {
      <label for={id} data-help={Unparsed(S.?(s"help.$id"))} data-required={required.toString}>
          {S.?(s"label.$id")}:{if (required) " *"}
      </label>
    }

    private def setupHeader(id: String): NodeSeq = {
      <span id={id} class="avyHeader">{S.?(s"header.$id")}</span>
    }
    
    private def setupMessage(id: String): NodeSeq = {
      <span id={id} class="avyMsg">{getMessage(id)}</span>
    }
    
    private def setupMenuItem(id: String): NodeSeq = {
      <a id={id} class="avyMenuItem">{S.?(s"link.$id")}</a>
    }
    
    private def getButton(id: String) = S.?(s"button.$id")
}