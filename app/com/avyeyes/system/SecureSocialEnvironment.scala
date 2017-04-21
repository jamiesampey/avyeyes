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
class SecureSocialEnvironment @Inject() (val configuration: Configuration, val messagesApi: MessagesApi, avyEyesUserService: AvyEyesUserService, eventListener: SecureSocialEventListener)
  extends RuntimeEnvironment.Default {

  type U = AvyEyesUser

  override implicit val executionContext = play.api.libs.concurrent.Execution.defaultContext
  override lazy val userService = avyEyesUserService
  override lazy val eventListeners = List(eventListener)
  override lazy val providers = ListMap(
    include(new UsernamePasswordProvider[U](userService, avatarService, viewTemplates, passwordHashers)),
    include(new FacebookProvider(routes, cacheService, oauth2ClientFor(FacebookProvider.Facebook))),
    include(new GoogleProvider(routes, cacheService, oauth2ClientFor(GoogleProvider.Google)))
  )
}
