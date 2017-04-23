package com.avyeyes.controllers

import javax.inject.Inject

import com.avyeyes.data.CachedDAL
import com.avyeyes.model.{Avalanche, AvyEyesUser}
import com.avyeyes.service.AvyEyesUserService.AdminRoles
import com.avyeyes.util.Constants.AvalancheEditWindow
import org.joda.time.{DateTime, Seconds}

class Authorizations @Inject()(dal: CachedDAL) {

  def isAuthorizedToView(user: Option[AvyEyesUser], extId: String): Boolean = isAdmin(user) || dal.getAvalanche(extId).exists(_.viewable)

  def isAuthorizedToEdit(user: Option[AvyEyesUser], editKey: Option[String], extId: String): Boolean = isAdmin(user) || editKeyIsValid(dal.getAvalanche(extId), editKey)

  private def isAdmin(user: Option[AvyEyesUser]) = user.map(_.roles).exists(AdminRoles.contains)

  private def editKeyIsValid(avalancheOpt: Option[Avalanche], editKeyOpt: Option[String]) = (avalancheOpt, editKeyOpt) match {
    case (Some(avalanche), Some(editKey)) if avalanche.editKey == editKey.toLong =>
      Seconds.secondsBetween(avalanche.createTime, DateTime.now).getSeconds < AvalancheEditWindow.toSeconds
    case _ => false
  }
}
