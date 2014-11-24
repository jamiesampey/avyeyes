package com.avyeyes.model.enums

import scala.collection.mutable.ListBuffer
import net.liftweb.json.JsonAST._
import net.liftweb.json.Printer
import net.liftweb.http.S

trait UISelectableEnum {
  this: Enumeration =>
      
  private val JQAC_LABEL = "label"
  private val JQAC_VALUE = "value"
  private val UNKNOWN_CODE = "U"
  
	def toAutoCompleteSourceJson(): String = {
		val listBuffer = new ListBuffer[JObject]()
		values.foreach(v => listBuffer.append(toJObject(v)))
		Printer.compact(render(JArray(listBuffer.toList)))
	}
  
  def toJObject(v: Enumeration#Value): JObject = JObject(List(
    JField(JQAC_VALUE, JString(v.toString)),
    JField(JQAC_LABEL, JString(getAutoCompleteLabel(v))) 
  ))

  def isCompositeLabel = false
    
  private def getAutoCompleteLabel(value: Enumeration#Value) = isCompositeLabel match {
    case true => s"${value.toString} - ${getLabel(value)}"
    case false => getLabel(value)
  }
  
  def getLabel(value: Enumeration#Value): String = {
    val name = value.toString
    if (name == UNKNOWN_CODE)
      S.?(s"enum.$name")
    else {
      val enumClass = getClass.getSimpleName filterNot(c => c == '$')
      S.?(s"enum.$enumClass.$name")
    }
  }
}