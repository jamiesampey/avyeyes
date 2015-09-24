package com.avyeyes.model.enums

abstract class AutocompleteEnum extends Enumeration {
  def default: this.Value

  def withCode(code: String) = values.find(_.toString.endsWith(s".$code")).getOrElse(default)

  def isValidCode(code: String): Boolean = !code.isEmpty && values.exists(_.toString.endsWith(code))
}
