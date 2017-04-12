package com.avyeyes.controllers

import javax.inject._

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import org.webjars.play.RequireJS

@Singleton
class TemplateController @Inject()(requireJS: RequireJS, val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def index = Action { implicit request =>
    Ok(com.avyeyes.views.html.index(requireJS))
  }

  def admin = Action { implicit request =>
    Ok(com.avyeyes.views.html.admin(requireJS))
  }
}
