package com.avyeyes.test

import bootstrap.liftweb.Boot
import scala.xml.XML

abstract class AvyEyesSpec extends WebSpec2(Boot().boot _) {
  lazy val IndexHtmlElem = XML.loadFile("src/main/webapp/index.html")
}