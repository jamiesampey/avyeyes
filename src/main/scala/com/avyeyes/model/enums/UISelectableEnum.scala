package com.avyeyes.model.enums

import scala.collection.mutable.ListBuffer
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL
import net.liftweb.json.Printer
import net.liftweb.util.Props
import net.liftweb.http.S
import com.avyeyes.util.AEHelpers._

trait UISelectableEnum {
    this: Enumeration =>
      
    private val JQAC_LABEL = "label"
    private val JQAC_VALUE = "value"
    
	def toJsonArray(): String = {
		val listBuffer = new ListBuffer[JObject]()
		values.foreach(v =>
		    listBuffer.append(JObject(List(
		        JField(JQAC_VALUE, JString(v.toString)),
		        JField(JQAC_LABEL, JString(getEnumLabel(this, v.toString))) 
		    )))
		)
		Printer.compact(JsonAST.render(JArray(listBuffer.toList)))
	}
}