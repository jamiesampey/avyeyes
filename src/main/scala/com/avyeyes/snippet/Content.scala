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
      "label" #> ((ns:NodeSeq) => setupLabel((ns\"@for").text)) &
      ".avyHeader" #> ((n:NodeSeq) => setupHeader((n\"@id").text)) &
      ".avyMsg" #> ((n:NodeSeq) => getMessage((n\"@id").text)) &
      ".avyButton [value]" #> ((n:NodeSeq) => getButton((n\"@id").text)) &
      "#avySearchHelpLink *" #> S.?("link.avySearchHelpLink") & 
      "#reportMenuItem *" #> S.?("link.reportMenuItem") &
      "#aboutMenuItem *" #> S.?("link.aboutMenuItem") 
    }

    private def setupLabel(id: String): NodeSeq = {
      <label for={id} data-help={Unparsed(S.?(s"help.$id"))}>{S.?(s"label.$id")}</label>
    }

    private def setupHeader(id: String): NodeSeq = {
      <span id={id} class="avyHeader">{S.?(s"header.$id")}</span>
    }
    
    private def getButton(id: String) = S.?(s"button.$id")
}