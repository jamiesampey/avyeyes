package com.avyeyes.service

import net.liftweb.http.S
import net.liftweb.util.Props

import scala.xml.Unparsed

class ResourceService {
  def getProperty(prop: String) = Props.get(prop) openOr(prop)// openOrThrowException(s"Property $prop was not found in any properties file!")
  def getIntProperty(prop: String) = Props.getInt(prop) openOr(-1)
  def getBooleanProperty(prop: String) = Props.getBool(prop) openOr(false)

  def getMessage(id: String, params: Any*) = Unparsed(S.?(s"msg.$id", params:_*))
  
  def getHttpsBaseUrl = {
    val portSuffix = getProperty("httpsPort") match {
      case "443" => ""
      case p => s":$p"
    }
    s"https://${getProperty("hostname")}$portSuffix"
  }

  def getAvalancheUrl(extId: String) = s"${getHttpsBaseUrl}/$extId"
}
