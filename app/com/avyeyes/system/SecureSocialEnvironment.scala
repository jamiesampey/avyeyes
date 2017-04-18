package com.avyeyes.system

import javax.inject.{Inject, Singleton}

import com.avyeyes.service.AvyEyesUserService
import play.api.Configuration
import play.api.i18n.MessagesApi
import securesocial.core.{BasicProfile, RuntimeEnvironment}


@Singleton
class SecureSocialEnvironment @Inject() (val configuration: Configuration, val messagesApi: MessagesApi, avyEyesUserService: AvyEyesUserService, eventListener: SecureSocialEventListener) extends RuntimeEnvironment.Default {
  type U = BasicProfile
  override implicit val executionContext = play.api.libs.concurrent.Execution.defaultContext
  override lazy val userService = avyEyesUserService
  override lazy val eventListeners = List(eventListener)
}
