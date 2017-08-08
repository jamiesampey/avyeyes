package com.jamiesampey.avyeyes.controllers

import javax.inject._

import com.jamiesampey.avyeyes.data.CachedDao
import com.jamiesampey.avyeyes.model.enums._
import com.jamiesampey.avyeyes.service.AvyEyesUserService.AdminRoles
import com.jamiesampey.avyeyes.service.ConfigurationService
import com.jamiesampey.avyeyes.system.UserEnvironment
import org.json4s.JsonAST
import org.json4s.JsonDSL._
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import securesocial.core.SecureSocial


@Singleton
class TemplateController @Inject()(val configService: ConfigurationService, val logger: Logger, val messagesApi: MessagesApi,
                                   val dao: CachedDao, authorizations: Authorizations, implicit val env: UserEnvironment)
  extends SecureSocial with I18nSupport with Json4sMethods {

  import authorizations._
  private val s3Bucket = configService.getProperty("s3.bucket")

  def index(extId: String, editKeyOpt: Option[String]) = UserAwareAction { implicit request =>
    val avalancheJsonOpt: Option[String] = if (isAuthorizedToView(extId, request.user))
      dao.getAvalanche(extId).map(a => writeJson(avalanchePathSearchResult(a))) else None

    Ok(com.jamiesampey.avyeyes.views.html.index(autocompleteSources, s3Bucket, avalancheJsonOpt, if (isAdmin(request.user)) request.user else None))
  }

  def admin = SecuredAction(WithRole(AdminRoles)) { implicit request =>
    Ok(com.jamiesampey.avyeyes.views.html.admin(request.user))
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