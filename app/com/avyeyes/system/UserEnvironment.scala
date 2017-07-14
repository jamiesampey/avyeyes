package com.avyeyes.system

import javax.inject.{Inject, Singleton}

import com.avyeyes.model.AvyEyesUser
import com.avyeyes.service.AvyEyesUserService
import play.api.Configuration
import play.api.i18n.MessagesApi
import securesocial.core.RuntimeEnvironment
import securesocial.core.providers._

import scala.collection.immutable.ListMap


@Singleton
class UserEnvironment @Inject()(val configuration: Configuration, val messagesApi: MessagesApi, avyEyesUserService: AvyEyesUserService, eventListener: UserEventListener)
  extends RuntimeEnvironment.Default {

  override type U = AvyEyesUser

  override implicit val executionContext = play.api.libs.concurrent.Execution.defaultContext
  override lazy val userService = avyEyesUserService
  override lazy val eventListeners = List(eventListener)
  override lazy val providers = ListMap(include(new UsernamePasswordProvider[U](userService, avatarService, viewTemplates, passwordHashers)))
}
