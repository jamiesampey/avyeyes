package com.avyeyes.model.enums

import net.liftweb.http.S
import net.liftweb.json.JsonAST.{JString, JField, JObject, JValue}
import net.liftweb.json.{TypeInfo, Formats, Serializer}

import scala.reflect.ClassTag

abstract class AutocompleteEnum extends Enumeration {
  def isCompositeLabel = false
}

class AutocompleteEnumSerializer[E <: AutocompleteEnum: ClassTag](enum: E) extends Serializer[E#Value] {
  private val JQAC_LABEL = "label"
  private val JQAC_VALUE = "value"
  private val UNKNOWN_CODE = "U"

  val EnumerationClass = classOf[E#Value]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), E#Value] = {
    case (TypeInfo(EnumerationClass, _), json) => json match {
      case str: JValue => enum.withName((str \ JQAC_VALUE).extract[String])
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case v: E#Value => JObject(List(
      JField(JQAC_VALUE, JString(v.toString)),
      JField(JQAC_LABEL, JString(getAutoCompleteLabel(v)))
    ))
  }

  private def getAutoCompleteLabel(value: Enumeration#Value) = enum.isCompositeLabel match {
    case true => s"${value.toString} - ${getLabel(value)}"
    case false => getLabel(value)
  }

  def getLabel(value: Enumeration#Value): String = {
    val name = value.toString
    if (name == UNKNOWN_CODE)
      S.?(s"enum.$name")
    else {
      val enumClass = enum.getClass.getSimpleName filterNot(c => c == '$')
      S.?(s"enum.$enumClass.$name")
    }
  }
}