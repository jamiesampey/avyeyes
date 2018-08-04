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
import securesocial.core.SecureSocial


@Singleton
class TemplateController @Inject()(val configService: ConfigurationService, val log: Logger, val messagesApi: MessagesApi,
                                   val dao: CachedDao, authorizations: Authorizations, implicit val env: UserEnvironment)
  extends SecureSocial with I18nSupport with Json4sMethods {

  import authorizations._

  def index(extId: String) = UserAwareAction { implicit request =>
    Ok(com.jamiesampey.avyeyes.views.html.index(if (isAdmin(request.user)) request.user else None))
  }

  def admin = SecuredAction(WithRole(AdminRoles)) { implicit request =>
    Ok(com.jamiesampey.avyeyes.views.html.admin(request.user))
  }

  def clientDataBundle = UserAwareAction { implicit request => Ok(writeJson(s3config ~ dataCodes)) }

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
    (enumSimpleName(ModeOfTravel) -> enumToJsonArray(ModeOfTravel)) ~
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
}