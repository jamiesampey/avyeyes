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
    val errorMsg = s"Property '$prop' was not found in the properties file."
    logger.error(errorMsg)
    throw new RuntimeException(errorMsg)
  }

  def httpsBaseUrl: String = {
    val host = config.getString("hostname").getOrElse("avyeyes.com")
    val portSuffix = config.getInt("httpsPort").getOrElse(443) match {
      case 443 => ""
      case port => s":$port"
    }

    s"https://$host$portSuffix"
  }

  def adminLoginUrl = s"$httpsBaseUrl/$LoginPath"

  def avalancheUrl(extId: String) = s"$httpsBaseUrl/$extId"

  def avalancheEditUrl(a: Avalanche) = s"${avalancheUrl(a.extId)}?edit=${a.editKey}"
}
