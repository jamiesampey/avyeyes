package com.avyeyes.snippet

import com.avyeyes.service.ResourceService
import net.liftweb.http.js.JE.Call
import net.liftweb.http.js.JsCmd

trait ModalDialogs {
  protected val R: ResourceService
  
	def infoDialog(msgId: String, params: Any*) = getDialogJsCmd("title.infoDialog", msgId, params:_*)
	def errorDialog(msgId: String, params: Any*) = getDialogJsCmd("title.errorDialog", msgId, params:_*)

	private def getDialogJsCmd(titleId: String, msgId: String, params: Any*): JsCmd = {
	  Call("avyEyesView.showModalDialog", R.localizedString(titleId), R.localizedString(s"msg.$msgId", params:_*)).cmd
	}
}
