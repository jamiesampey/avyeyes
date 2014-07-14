package com.avyeyes.snippet

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.http.S
import scala.xml.NodeSeq
import scala.xml.Unparsed
import com.avyeyes.util.AEHelpers._

class Content {
    def render = {
      "label" #> ((n:NodeSeq) => getLabel((n\"@for").text)) &
      ".avyHeader" #> ((n:NodeSeq) => getHeader((n\"@id").text)) &
      ".avyMsg" #> ((n:NodeSeq) => getMessage((n\"@id").text)) &
      ".avyButton [value]" #> ((n:NodeSeq) => getButton((n\"@id").text)) &
      "#avySearchHelpLink *" #> S.?("link.avySearchHelpLink") & 
      "#reportMenuItem *" #> S.?("link.reportMenuItem") &
      "#aboutMenuItem *" #> S.?("link.aboutMenuItem") 
    }

    private def getLabel(id: String) = S.?(s"label.$id") + ":"
    private def getHeader(id: String) = S.?(s"header.$id")
    private def getButton(id: String) = S.?(s"button.$id")
}