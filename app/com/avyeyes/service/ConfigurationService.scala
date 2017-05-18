package com.avyeyes.service

import javax.inject.Inject

import com.avyeyes.model.Avalanche
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

  private lazy val httpBaseUrl: String = {
    val host = config.getString("hostname").getOrElse("avyeyes.com")
    val portSuffix = config.getInt("httpPort").getOrElse(80) match {
      case 80 => ""
      case port => s":$port"
    }

    s"http://$host$portSuffix"
  }

  def avalancheUrl(extId: String) = s"$httpBaseUrl/$extId"

  def avalancheEditUrl(a: Avalanche) = s"${avalancheUrl(a.extId)}?edit=${a.editKey}"
}
