package com.avyeyes.controllers

import javax.inject._

import com.avyeyes.model.enums._
import com.avyeyes.service.AvyEyesUserService.AdminRoles
import com.avyeyes.service.ConfigurationService
import com.avyeyes.system.UserEnvironment
import org.json4s.JsonAST
import org.json4s.JsonDSL._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import securesocial.core.SecureSocial


@Singleton
class TemplateController @Inject()(val configService: ConfigurationService, val messagesApi: MessagesApi, implicit val env: UserEnvironment)
  extends SecureSocial with I18nSupport with Json4sMethods {

  private val s3Bucket = configService.getProperty("s3.bucket")

  def index(extId: String) = UserAwareAction { implicit request =>
    Ok(com.avyeyes.views.html.index(autocompleteSources, s3Bucket))
  }

  def admin = SecuredAction(WithRole(AdminRoles)) { implicit request =>
    Ok(com.avyeyes.views.html.admin())
  }

  private val autocompleteSources = Map(
    enumSimpleName(AvalancheType) -> enumToJsonArray(AvalancheType),
    enumSimpleName(AvalancheTrigger) -> enumToJsonArray(AvalancheTrigger),
    enumSimpleName(AvalancheTriggerModifier) -> enumToJsonArray(AvalancheTriggerModifier),
    enumSimpleName(AvalancheInterface) -> enumToJsonArray(AvalancheInterface),
    enumSimpleName(Direction) -> enumToJsonArray(Direction),
    enumSimpleName(WindSpeed) -> enumToJsonArray(WindSpeed),
    enumSimpleName(ModeOfTravel) -> enumToJsonArray(ModeOfTravel),
    enumSimpleName(ExperienceLevel) -> enumToJsonArray(ExperienceLevel)
  )

  private def enumToJsonArray(acEnum: AutocompleteEnum): String = {
    def getLocalizedLabel(tokens: Array[String]): String = {
      val label = if (tokens.last == "empty") "" else Messages(s"enum.${tokens.mkString(".")}")
      if (CompositeLabelEnums.contains(tokens.head)) s"${tokens.last} - $label" else label
    }

    val jValues: Seq[JsonAST.JObject] = acEnum.selectableValues.map { enumValue =>
      val tokens = enumValue.toString.split('.')
      tokens.length match {
        case 2 => ("label" -> getLocalizedLabel(tokens)) ~ ("value" -> tokens(1))
        case 3 => ("category" -> Messages(s"enum.${tokens(0)}.${tokens(1)}")) ~ ("label" -> getLocalizedLabel(tokens)) ~ ("value" -> tokens(2))
      }
    }

    writeJson(jValues)
  }
}