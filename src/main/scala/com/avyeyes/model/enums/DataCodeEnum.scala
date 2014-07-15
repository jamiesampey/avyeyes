package com.avyeyes.model.enums

import scala.collection.mutable.ListBuffer
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL
import net.liftweb.json.Printer
import net.liftweb.http.js.JsExp
import net.liftweb.http.js.JsObj
import net.liftweb.http.js.JE.JsArray
import net.liftweb.util.Props
import net.liftweb.http.S

abstract class DataCodeEnum extends Enumeration {
    private val JQAC_LABEL = "label"
    private val JQAC_VALUE = "value"
      
    private val UNKNOWN_CODE = "U"
    val U: Value
    
	def toJsonArray(): String = {
		val listBuffer = new ListBuffer[JObject]()
		values.foreach(v => 
		    listBuffer.append(JObject(List(
		        JField(JQAC_LABEL, JString(getEnumLabel(v.toString.toUpperCase))), 
		        JField(JQAC_VALUE, JString(v.toString)))
		    ))
		)
		Printer.compact(JsonAST.render(JArray(listBuffer.toList)))
	}
	
	def withCode(c: String): Value = Option(c) match {
		case Some(c) if (!c.isEmpty) => values.find(_.toString == c).get
		case _ => U
	}
	
	def getEnumLabel(code: String): String = {
        if (code == UNKNOWN_CODE)
            S.?("enum.U")
        else
            getClass.getSimpleName match {
                case "AvalancheType$" => S.?(s"enum.type.$code")
                case "AvalancheTrigger$" => S.?(s"enum.trigger.$code")
                case "AvalancheInterface$" => S.?(s"enum.interface.$code")
                case "Aspect$" => S.?(s"enum.aspect.$code")
                case "Sky$" => S.?(s"enum.sky.$code")
                case "Precip$" => S.?(s"enum.precip.$code")
                case "ModeOfTravel$" => S.?(s"enum.travel.$code")
            }
    }
}