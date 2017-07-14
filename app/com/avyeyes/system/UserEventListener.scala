package com.avyeyes.system

import com.avyeyes.model.AvyEyesUser
import play.api.Logger
import play.api.mvc.{RequestHeader, Session}
import securesocial.core._

class UserEventListener extends EventListener {

  def onEvent[U](event: Event[U], request: RequestHeader, session: Session): Option[Session] = {
    event match {
      case LoginEvent(u: AvyEyesUser) => Logger.info(s"LOGIN event for ${u.email}")
      case LogoutEvent(u: AvyEyesUser) => Logger.info(s"LOGOUT event for ${u.email}")
      case SignUpEvent(u: AvyEyesUser) => Logger.info(s"SIGNUP event for ${u.email}")
    }
    None
  }
}
