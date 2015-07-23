package com.avyeyes.model

import com.avyeyes.model.enums._
import net.liftweb.json.ext.DateTimeSerializer
import net.liftweb.json.{DefaultFormats, Formats}

object JsonFormats {
  implicit val formats: Formats = new DefaultFormats {} +
    DateTimeSerializer +
    new AutocompleteEnumSerializer(Aspect) +
    new AutocompleteEnumSerializer(AvalancheInterface) +
    new AutocompleteEnumSerializer(AvalancheTrigger) +
    new AutocompleteEnumSerializer(AvalancheType) +
    new AutocompleteEnumSerializer(ExperienceLevel) +
    new AutocompleteEnumSerializer(ModeOfTravel) +
    new AutocompleteEnumSerializer(SkyCoverage) +
    new AutocompleteEnumSerializer(Precipitation) +
    AvalancheImageJsonSerializer +
    Classification.JsonSerializer +
    Coordinate.JsonSerializer +
    HumanNumbers.JsonSerializer +
    Scene.JsonSerializer +
    Slope.JsonSerializer
}
