package com.avyeyes.service

import javax.inject.Inject

import com.avyeyes.model.Avalanche
import com.avyeyes.util.Constants.LoginPath
import play.api.{Configuration, Logger}


class ConfigurationService @Inject()(config: Configuration, logger: Logger)() {

  def getProperty(prop: String): String = config.getString(prop) getOrElse logErrorAndThrowException(prop)
  def getIntProperty(prop: String): Int = config.getInt(prop) getOrElse logErrorAndThrowException(prop)
  def getBooleanProperty(prop: String): Boolean = config.getBoolean(prop) getOrElse logErrorAndThrowException(prop)

  private def logErrorAndThrowException(prop: String) = {
    logger.error(s"Property '$prop' was not found in the properties file.")
    throw new RuntimeException("Ruh roh, an exception occurred and the code bailed!")
  }

  def httpsBaseUrl: String = {
    val portSuffix = config.getInt("httpsPort") match {
      case Some(443) => ""
      case p => s":$p"
    }
    s"https://${config.getString("hostname")}$portSuffix"
  }

  def adminLoginUrl = s"$httpsBaseUrl/$LoginPath"

  def avalancheUrl(extId: String) = s"$httpsBaseUrl/$extId"

  def avalancheEditUrl(a: Avalanche) = s"${avalancheUrl(a.extId)}?edit=${a.editKey}"
}
