package avyeyes.util.ui

import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JE.Call
import net.liftweb.http.js.JsExp.strToJsExp

object JsDialog {
	def info(msg: String): JsCmd = Call("avyEyesView.showModalDialog", "Info", msg).cmd
    def error(msg: String): JsCmd = error(msg, null)
	def error(msg: String, details: String): JsCmd = Call("avyEyesView.showModalDialog", "Error", msg, details).cmd
}
