package com.avyeyes.system

import securesocial.core._
import play.api.mvc.{ Session, RequestHeader }
import play.api.Logger

/**
  * A sample event listener
  */

class SecureSocialEventListener extends EventListener {

  def onEvent[U](event: Event[U], request: RequestHeader, session: Session): Option[Session] = {
    val eventName = event match {
      case LoginEvent(u) => "login"
      case LogoutEvent(u) => "logout"
    }

    event match {
      case Event(u: BasicProfile) => Logger.info(s"$eventName event for user ${u.userId}")
    }

    // Not changing the session so just return None
    // if you wanted to change the session then you'd do something like
    // Some(session + ("your_key" -> "your_value"))
    None
  }
}
