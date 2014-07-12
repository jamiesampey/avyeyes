package com.avyeyes.snippet

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.http.S
import scala.xml.NodeSeq

class Content {
    def render = {
      "label" #> ((n:NodeSeq) => getLabel((n\"@for").text))
    }
    
    private def getLabel(id: String) = S.?(s"label.$id") + ":"
}