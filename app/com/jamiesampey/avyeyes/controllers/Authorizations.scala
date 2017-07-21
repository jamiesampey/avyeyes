package com.jamiesampey.avyeyes.controllers

import javax.inject.Inject

import com.jamiesampey.avyeyes.data.CachedDao
import com.jamiesampey.avyeyes.model.{Avalanche, AvyEyesUser, AvyEyesUserRole}
import com.jamiesampey.avyeyes.service.AvyEyesUserService.AdminRoles
import com.jamiesampey.avyeyes.service.ExternalIdService
import com.jamiesampey.avyeyes.util.Constants.AvalancheEditWindow
import org.joda.time.{DateTime, Seconds}
import play.api.mvc.RequestHeader
import securesocial.core.Authorization

class Authorizations @Inject()(dao: CachedDao, idService: ExternalIdService) {

  def isAuthorizedToView(extId: String, user: Option[AvyEyesUser]): Boolean = {
    isAdmin(user) || dao.getAvalanche(extId).exists(_.viewable)
  }

  def isAuthorizedToEdit(extId: String, user: Option[AvyEyesUser], editKey: Option[String]): Boolean = {
    isAdmin(user) || idService.reservationExists(extId) || editKeyIsValid(dao.getAvalanche(extId), editKey)
  }

  def isAdmin(user: Option[AvyEyesUser]) = user.map(_.roles).exists(userRoles => userRoles.exists(AdminRoles.contains))

  private def editKeyIsValid(avalancheOpt: Option[Avalanche], editKeyOpt: Option[String]) = (avalancheOpt, editKeyOpt) match {
    case (Some(avalanche), Some(editKey)) if avalanche.editKey == editKey.toLong =>
      Seconds.secondsBetween(avalanche.createTime, DateTime.now).getSeconds < AvalancheEditWindow.toSeconds
    case _ => false
  }
}

case class WithRole(authorizedRoles: List[AvyEyesUserRole]) extends Authorization[AvyEyesUser] {
  def isAuthorized(user: AvyEyesUser, request: RequestHeader) = user.roles.exists(authorizedRoles.contains)
}
