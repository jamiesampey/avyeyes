package com.avyeyes.snippet

import com.avyeyes.service.Injectors
import net.liftweb.http.S
import net.liftweb.util.Helpers._

import scala.xml._

class Content {
  val userSession = Injectors.user.vend
  val R = Injectors.resources.vend

  def render = {
    "label" #> ((ns:NodeSeq) => setupLabel((ns\"@for").text, asBoolean((ns\"@data-required").text) openOr false)) &
    ".avyHeader" #> ((n:NodeSeq) => setupHeader((n\"@id").text)) &
    ".avyMsg" #> ((n:NodeSeq) => setupMessage((n\"@id").text)) &
    ".avyLink" #> ((n:NodeSeq) => setupLink((n\"@id").text)) &
    ".avyButton [value]" #> ((n:NodeSeq) => getButton((n\"@id").text)) &
    ".avyAdminLoggedInDiv" #> getAdminLoggedInDiv
  }

  private def setupLabel(id: String, required: Boolean) = Utility.trim(
    <label for={id} data-help={Unparsed(S.?(s"help.$id"))} data-required={required.toString}>
        {S.?(s"label.$id")}:{if (required) Unparsed("<span style='color: red;'>&nbsp;*</span>")}
    </label>
  )

  private def setupHeader(id: String) = <span id={id} class="avyHeader">{S.?(s"header.$id")}</span>

  private def setupMessage(id: String) = <span id={id} class="avyMsg">{R.localizedStringAsXml(s"msg.$id")}</span>

  private def setupLink(id: String) = <a id={id} class="avyLink">{S.?(s"link.$id")}</a>

  private def getButton(id: String) = S.?(s"button.$id")

  private def getAdminLoggedInDiv = userSession.isAdminSession match {
    case false => NodeSeq.Empty
    case true => {
      <div class="avyAdminLoggedInDiv">
        <form class="lift:Admin.logOut?form=post">
          <label for="avyAdminLoggedInEmail">{S.?("label.avyAdminLoggedInEmail")}</label>
          <span id="avyAdminLoggedInEmail">{userSession.authorizedEmail}</span>
          <input id="avyAdminLogoutButton" type="submit" value={S.?("button.avyAdminLogOutButton")} />
        </form>
      </div>
    }
  }
}