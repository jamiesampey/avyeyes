package com.jamiesampey.avyeyes.service

import javax.inject.Inject

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
}
