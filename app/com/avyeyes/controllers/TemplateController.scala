package com.avyeyes.controllers

import javax.inject._

import com.avyeyes.model.enums._
import org.json4s.jackson.JsonMethods.{compact => json4sCompact, render => json4sRender}
import org.json4s.JsonDSL._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.Controller
import play.api.mvc.Action

@Singleton
class TemplateController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def index(extId: String) = Action { implicit request =>
    Ok(com.avyeyes.views.html.index(autocompleteSources))
  }

  def admin = Action { implicit request =>
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

    val jValues = acEnum.selectableValues.map { enumValue =>
      val tokens = enumValue.toString.split('.')
      tokens.length match {
        case 2 => ("label" -> getLocalizedLabel(tokens)) ~ ("value" -> tokens(1))
        case 3 => ("category" -> Messages(s"enum.${tokens(0)}.${tokens(1)}")) ~ ("label" -> getLocalizedLabel(tokens)) ~ ("value" -> tokens(2))
      }
    }

    json4sCompact(json4sRender(jValues))
  }
}
