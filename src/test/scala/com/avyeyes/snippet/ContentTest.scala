package com.avyeyes.snippet

import com.avyeyes.service.Injectors

import scala.xml._
import com.avyeyes.test._
import net.liftweb.http.S

class ContentTest extends WebSpec2 with TemplateReader {
  var renderedPage: NodeSeq = NodeSeq.Empty
  
  "Snippet rendering" should {
    "Wire label fields" withSFor("/") in {
      renderedPage = (new Content).render(IndexHtmlElem)
      val labels = renderedPage \\ "label"
      
      for (label <- labels) {
        val id = (label\"@for").text
        id.length() must be_>(0)
        (label\"@data-help").text must_== s"${Unparsed(S.?(s"help.$id"))}"
        label.text.trim must startWith(s"${S.?(s"label.$id")}:")
      }
      success
    }
    
    "Wire header fields" withSFor("/") in {
      val headers = renderedPage \\ "span" filter (node => (node\"@class").text == "avyHeader")
      
      for (header <- headers) {
        val id = (header\"@id").text
        id.length() must be_>(0)
        (header\"@class").text must_== "avyHeader"
        header.text.trim must_== s"${S.?(s"header.$id")}"
      }
      success
    }
    
    "Wire message fields" withSFor("/") in {
      val P = Injectors.resources.vend
      val messages = renderedPage \\ "span" filter (node => (node\"@class").text == "avyMsg")
      
      for (message <- messages) {
        val id = (message\"@id").text
        id.length() must be_>(0)
        (message\"@class").text must_== "avyMsg"
        message.text.trim must_== s"${P.getMessage(id)}".trim
      }
      success
    }
  
    "Wire link fields" withSFor("/") in {
      val links = renderedPage \\ "a" filter (node => (node\"@class").text == "avyLink")
      
      for (link <- links) {
        val id = (link\"@id").text
        id.length() must be_>(0)
        (link\"@class").text must_== "avyLink"
        link.text.trim must_== s"${S.?(s"link.$id")}"
      }
      success
    }
    
    "Wire button fields" withSFor("/") in {
      val buttons = renderedPage \\ "input" filter (node => (node\"@class").text == "avyButton")
      
      for (button <- buttons) {
        val id = (button\"@id").text
        id.length() must be_>(0)
        (button\"@class").text must_== "avyButton"
        (button\"@value").text must_== s"${S.?(s"button.$id")}"
      }
      success
    }
  }
}