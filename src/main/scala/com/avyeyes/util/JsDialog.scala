package com.avyeyes.util

import com.avyeyes.service.Injectors
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JE.Call
import net.liftweb.http.S

object JsDialog {
  val R = Injectors.resources.vend

	def info(msgId: String, params: Any*) = getDialogJsCmd("title.infoDialog", msgId, params:_*)
	def error(msgId: String, params: Any*) = getDialogJsCmd("title.errorDialog", msgId, params:_*)

	private def getDialogJsCmd(titleId: String, msgId: String, params: Any*): JsCmd = {
	  Call("avyEyesView.showModalDialog", S.?(titleId), R.getMessage(msgId, params:_*).toString).cmd
	}
}
