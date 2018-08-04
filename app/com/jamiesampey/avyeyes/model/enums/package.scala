package com.jamiesampey.avyeyes.model

package object enums {
  def enumSimpleName(enum: AutocompleteEnum): String = {
    val name = enum.getClass.getSimpleName.replace("$", "")
    s"${name.head.toLower}${name.tail}" // camelCase
  }
}
