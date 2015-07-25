package com.avyeyes.model.enums

abstract class AutocompleteEnum extends Enumeration {
  def default: this.Value
  def withCode(code: String) = values.find(_.toString.endsWith(code)).getOrElse(default)
}
