package com.avyeyes.model.enums

import scala.collection.mutable.ListBuffer

import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL
import net.liftweb.json.Printer

import net.liftweb.http.js.JsExp
import net.liftweb.http.js.JsObj
import net.liftweb.http.js.JE.JsArray

import com.avyeyes.util.AEConstants._

abstract class DataCodeEnum extends Enumeration {
	case class DataCodeVal(idx: Int, code: String, desc: String) extends Val(idx, code) {
	  def this(idx: Int, code: String) = this(idx, code, "")
	  override def toString = if (desc.isEmpty()) code else code + " - " + desc
	}
	
	implicit def valueToDataCodeVal(v: Value): DataCodeVal = v.asInstanceOf[DataCodeVal]
	
	def toJsonArray(): String = {
		val listBuffer = new ListBuffer[JObject]()
		values.foreach(dc => 
		    listBuffer.append(JObject(
		        List(JField(JQAC_LABEL, JString(dc.toString)), JField(JQAC_VALUE, JString(dc.code)))))
		    )
		Printer.compact(JsonAST.render(JArray(listBuffer.toList)))
	}
	   
	val UNKNOWN = new DataCodeVal(0, UNKNOWN_CODE, UNKNOWN_LABEL)
	
	def withCode(c: String): DataCodeVal = Option(c) match {
		case Some(c) if (!c.isEmpty) => values.find(_.code == c).get
		case _ => UNKNOWN
	}
}