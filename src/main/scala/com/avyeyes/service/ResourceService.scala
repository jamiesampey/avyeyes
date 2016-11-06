package com.avyeyes.service

import com.avyeyes.model.Avalanche
import com.avyeyes.util.Constants.LoginPath
import net.liftweb.common.Loggable
import net.liftweb.http.S
import net.liftweb.util.Props

import scala.xml.Unparsed

class ResourceService extends Loggable {

  def getProperty(prop: String) = Props.get(prop) openOr logErrorAndThrowException(prop)
  def getIntProperty(prop: String) = Props.getInt(prop) openOr logErrorAndThrowException(prop)
  def getBooleanProperty(prop: String) = Props.getBool(prop) openOr logErrorAndThrowException(prop)

  private def logErrorAndThrowException(prop: String) = {
    logger.error(s"Property '$prop' was not found in the properties file.")
    throw new RuntimeException("Ruh roh, an exception occurred and the code bailed!")
  }

  def localizedString(id: String, params: Any*) = S.?(id, params:_*)
  def localizedStringAsXml(id: String, params: Any*) = Unparsed(localizedString(id, params:_*))
  
  def httpsBaseUrl = {
    val portSuffix = getProperty("httpsPort") match {
      case "443" => ""
      case p => s":$p"
    }
    s"https://${getProperty("hostname")}$portSuffix"
  }

  def adminLoginUrl = s"$httpsBaseUrl/$LoginPath"

  def avalancheUrl(extId: String) = s"$httpsBaseUrl/$extId"

  def avalancheEditUrl(a: Avalanche) = s"${avalancheUrl(a.extId)}?edit=${a.editKey}"
}
