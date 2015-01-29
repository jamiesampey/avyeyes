package com.avyeyes.service

import com.avyeyes.util.UserSession
import net.liftweb.http.Factory

object UserInjector extends Factory {
  val userSession = new FactoryMaker[UserSession](new UserSession) {}
}