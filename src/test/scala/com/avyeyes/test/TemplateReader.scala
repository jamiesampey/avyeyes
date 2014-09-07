package com.avyeyes.test

import scala.xml.XML

trait TemplateReader {
  lazy val IndexHtmlElem = XML.loadFile("src/main/webapp/index.html")
  lazy val WhaWhaHtmlElem = XML.loadFile("src/main/webapp/whawha.html")
}