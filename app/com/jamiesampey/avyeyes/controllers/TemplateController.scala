package com.jamiesampey.avyeyes.controllers

import com.jamiesampey.avyeyes.data.CachedDao
import com.jamiesampey.avyeyes.model.enums._
import com.jamiesampey.avyeyes.service.AvyEyesUserService.AdminRoles
import com.jamiesampey.avyeyes.service.ConfigurationService
import com.jamiesampey.avyeyes.system.UserEnvironment
import javax.inject._
import org.json4s.JsonDSL._
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.Action
import securesocial.core.SecureSocial


@Singleton
class TemplateController @Inject()(val configService: ConfigurationService, val log: Logger, val messagesApi: MessagesApi,
                                   val dao: CachedDao, authorizations: Authorizations, implicit val env: UserEnvironment)
  extends SecureSocial with I18nSupport with Json4sMethods {

  def index(extId: String) = Action { implicit request =>
    Ok(com.jamiesampey.avyeyes.views.html.index())
  }

  def admin = SecuredAction(WithRole(AdminRoles)) { implicit request =>
    Ok(com.jamiesampey.avyeyes.views.html.admin())
  }

  def clientDataBundle = Action { implicit request =>
    Ok(writeJson(s3config ~ dataCodes ~ tooltips ~ helpText))
  }

  def currentUser = UserAwareAction { implicit request =>
    Ok(writeJson("email" -> request.user.map(_.email)))
  }

    private val s3config = "s3" ->
    ("bucket" -> configService.getProperty("s3.bucket")) ~
    ("accessKeyId" -> configService.getProperty("s3.readonly.accessKeyId")) ~
    ("secretAccessKey" -> configService.getProperty("s3.readonly.secretAccessKey"))

  private val dataCodes = "codes" ->
    (enumSimpleName(AvalancheType) -> enumToJsonArray(AvalancheType)) ~
    (enumSimpleName(AvalancheTrigger) -> enumToJsonArray(AvalancheTrigger)) ~
    (enumSimpleName(AvalancheTriggerModifier) -> enumToJsonArray(AvalancheTriggerModifier)) ~
    (enumSimpleName(AvalancheInterface) -> enumToJsonArray(AvalancheInterface)) ~
    (enumSimpleName(Direction) -> enumToJsonArray(Direction)) ~
    (enumSimpleName(WindSpeed) -> enumToJsonArray(WindSpeed)) ~
    (enumSimpleName(ExperienceLevel) -> enumToJsonArray(ExperienceLevel))

  private def enumToJsonArray(acEnum: AutocompleteEnum) = {
    def toLocalizedLabel(tokens: Array[String]): String = Messages(s"enum.${tokens.mkString(".")}")

    acEnum.selectableValues.map { enumValue =>
      val tokens = enumValue.toString.split('.')
      tokens.length match {
        case 2 => ("label" -> toLocalizedLabel(tokens)) ~ ("value" -> tokens(1))
        case 3 => ("category" -> Messages(s"enum.${tokens(0)}.${tokens(1)}")) ~ ("label" -> toLocalizedLabel(tokens)) ~ ("value" -> tokens(2))
      }
    }
  }

  private val tooltips = "tooltips" -> messagesApi.messages("default").filterKeys(_.startsWith("tooltip")).map { case (k, v) => (k.split('.').last, v) }

  private val helpText = "help" -> messagesApi.messages("default").filterKeys(_.startsWith("help")).map { case (k, v) => (k.split('.').last, v) }
}