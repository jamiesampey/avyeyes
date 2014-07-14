package com.avyeyes.snippet

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.http.S
import scala.xml.NodeSeq
import scala.xml.Unparsed

class Content {
    def render = {
      "label" #> ((n:NodeSeq) => getLabel((n\"@for").text)) &
      ".avyHeader" #> ((n:NodeSeq) => getHeader((n\"@id").text)) &
      ".avyText" #> ((n:NodeSeq) => getText((n\"@id").text)) &
      ".avyButton [value]" #> ((n:NodeSeq) => getButton((n\"@id").text)) &
      "#avySearchHelpLink *" #> S.?("link.avySearchHelpLink") & 
      "#reportMenuItem *" #> S.?("link.reportMenuItem") &
      "#aboutMenuItem *" #> S.?("link.aboutMenuItem") 
    }
    
    private def getLabel(id: String) = S.?(s"label.$id") + ":"
    private def getHeader(id: String) = S.?(s"header.$id")
    private def getText(id: String) = Unparsed(S.?(s"text.$id"))
    private def getButton(id: String) = S.?(s"button.$id")
}