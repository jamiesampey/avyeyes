package com.avyeyes.controllers

import javax.inject.Inject

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.{Avalanche, AvyEyesUser, AvyEyesUserRole}
import com.avyeyes.service.AvyEyesUserService.AdminRoles
import com.avyeyes.service.ExternalIdService
import com.avyeyes.util.Constants.AvalancheEditWindow
import org.joda.time.{DateTime, Seconds}
import play.api.mvc.RequestHeader
import securesocial.core.Authorization

class Authorizations @Inject()(dal: CachedDAL, idService: ExternalIdService) {

  def isAuthorizedToView(extId: String, user: Option[AvyEyesUser]): Boolean = {
    isAdmin(user) || dal.getAvalanche(extId).exists(_.viewable)
  }

  def isAuthorizedToEdit(extId: String, user: Option[AvyEyesUser], editKey: Option[String]): Boolean = {
    isAdmin(user) || idService.reservationExists(extId) || editKeyIsValid(dal.getAvalanche(extId), editKey)
  }

  private def isAdmin(user: Option[AvyEyesUser]) = user.map(_.roles).exists(AdminRoles.contains)

  private def editKeyIsValid(avalancheOpt: Option[Avalanche], editKeyOpt: Option[String]) = (avalancheOpt, editKeyOpt) match {
    case (Some(avalanche), Some(editKey)) if avalanche.editKey == editKey.toLong =>
      Seconds.secondsBetween(avalanche.createTime, DateTime.now).getSeconds < AvalancheEditWindow.toSeconds
    case _ => false
  }
}

case class WithRole(authorizedRoles: List[AvyEyesUserRole]) extends Authorization[AvyEyesUser] {
  def isAuthorized(user: AvyEyesUser, request: RequestHeader) = user.roles.exists(authorizedRoles.contains)
}
