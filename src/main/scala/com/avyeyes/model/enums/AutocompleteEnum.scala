package com.avyeyes.model.enums

import net.liftweb.http.S

import scala.reflect.ClassTag

abstract class AutocompleteEnum extends Enumeration {
  def isCompositeLabel = false

  class AutocompleteEnumValue(name: String, val x : String) extends Val(nextId, name)
  protected final def Value(name: String, x : String): MyVal = new MyVal(name,x)

  def getEnumAutoCompleteLabel(value: AutocompleteEnum#Value) = {
    isCompositeLabel match {
      case true => s"${value.toString} - ${getEnumLabel(value)}"
      case false => getEnumLabel(value)
    }
  }

  def getEnumLabel[T <: AutocompleteEnum: ClassTag](enumValue: T#Value): String = {
    import scala.reflect.runtime.universe._

    val enumName = enumValue.toString

    enumName match {
      case "U" => S.?(s"enum.U")
      case _ => {
        val EnumerationClass = classOf[T#Value]
        val cls = enumValue.getClass
        val enumClassName = enumValue.getClass.getSimpleName.replace("$", "")
        S.?(s"enum.$enumClassName.$enumName")
      }
    }
  }
}
