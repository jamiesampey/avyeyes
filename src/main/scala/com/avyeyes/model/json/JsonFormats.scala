package com.avyeyes.model.json

import com.avyeyes.model.enums._
import net.liftweb.json.{DefaultFormats, Formats}

object JsonFormats {
  implicit val formats: Formats = new DefaultFormats {} +
    new EnumAutocompleteSerializer(Aspect) +
    new EnumAutocompleteSerializer(AvalancheInterface) +
    new EnumAutocompleteSerializer(AvalancheTrigger) +
    new EnumAutocompleteSerializer(AvalancheType) +
    new EnumAutocompleteSerializer(ExperienceLevel) +
    new EnumAutocompleteSerializer(ModeOfTravel) +
    new EnumAutocompleteSerializer(SkyCoverage) +
    new EnumAutocompleteSerializer(Precipitation)
}
