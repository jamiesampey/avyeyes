package com.avyeyes.controllers

import com.avyeyes.model.{AvyEyesUser, AvyEyesUserRole}
import play.api.mvc.RequestHeader
import securesocial.core.Authorization

case class WithRole(authorizedRoles: List[AvyEyesUserRole]) extends Authorization[AvyEyesUser] {
  def isAuthorized(user: AvyEyesUser, request: RequestHeader) = user.roles.exists(authorizedRoles.contains)
}
