package com.avyeyes.model.enums

abstract class AutocompleteEnum extends Enumeration {
  def default: this.Value

  def fromCode(code: String) = values.find(_.toString.endsWith(s".$code")).getOrElse(default)

  def toCode(enumValue: AutocompleteEnum#Value): String = enumValue.toString.split('.')(1)
  
  def isValidCode(code: String): Boolean = !code.isEmpty && values.exists(_.toString.endsWith(code))
}
