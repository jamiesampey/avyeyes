package bootstrap.liftweb

import akka.actor.ActorSystem
import com.avyeyes.data.DatabaseMaintenance
import com.avyeyes.rest._
import com.avyeyes.service.Injectors
import com.avyeyes.util.Constants._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.util.Vendor.valToVendor
import omniauth.Omniauth


/**
 * Companion object for unit testing
 */
object Boot {
  def apply() = {
    val boot = new Boot
    boot.test_(true)
    boot
  }
}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable {
  lazy val R = Injectors.resources.vend
  lazy val actorSystem = ActorSystem()

  def boot() = {
    logger.info("LIFT BOOT")
    
    LiftRules.addToPackages("com.avyeyes")
    
    val contextPaths = IndexPath :: BrowserNotSupportedPath :: LoginPath :: Nil
    val appMenus: List[Menu] = Menu(Loc("home", IndexPath :: Nil, "Home")) :: 
      Menu(Loc("browserNotSupported", BrowserNotSupportedPath :: Nil, "Browser Not Supported")) :: 
      Menu(Loc("logIn", LoginPath :: Nil, "Log In")) :: Nil
    val menus = appMenus ::: Omniauth.sitemap
    
    LiftRules.setSiteMap(SiteMap(menus:_*))

    // browser not supported redirect
    LiftRules.earlyResponse.append { (req: Req) =>
      !browserSupported(req) && !req.path.partPath.contains(BrowserNotSupportedPath) match {
        case true => Full(RedirectResponse(BrowserNotSupportedPath))
        case false => Empty
      }
    }

    // external ID URL extraction
    LiftRules.statelessRewrite.prepend {
      case RewriteRequest(ParsePath(extId :: Nil, "", _, false), GetRequest, _) if !contextPaths.contains(extId) => 
        RewriteResponse(ParsePath(IndexPath :: Nil, "", true, true), Map(ExtIdUrlParam -> extId))
    }
    
    // setup REST endpoints
    LiftRules.dispatch.append(new OmniAuthCallback)
    LiftRules.dispatch.append(new AdminTable)
    LiftRules.dispatch.append(new AvyDetails)
    LiftRules.dispatch.append(new Images)
    LiftRules.statelessDispatch.append(new ExtIdVendor)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    LiftRules.maxMimeFileSize = MaxImageSize
    LiftRules.maxMimeSize = MaxImageSize
    LiftRules.resourceNames = "text" :: "enum" :: "help" :: Nil
    
    Omniauth.init // grabs omniauth.* settings from props file
    
    if (!test) {
      import actorSystem.dispatcher
      import scala.concurrent.duration._

      actorSystem.scheduler.schedule(
        initialDelay = R.getProperty("db.maintenanceDelaySeconds").toInt seconds,
        interval = R.getProperty("db.maintenanceIntervalHours").toInt hours,
        receiver = actorSystem.actorOf(akka.actor.Props(new DatabaseMaintenance)),
        message = DatabaseMaintenance.run)
    }
  }
  
  ResourceServer.allow {
    case "js" :: _ => true
  	case "css" :: _ => true
  }
  
  private def browserSupported(req: Req): Boolean = {
    logger.info(s"UserAgent is '${req.userAgent.openOr("<empty box>")}'")

    if ((req.isChrome && req.chromeVersion.openOr(0.0) < ChromeMinVersion) ||
      (req.isFirefox && req.firefoxVersion.openOr(0.0) < FirefoxMinVersion) ||
      (req.isIE && req.ieVersion.openOr(0) < IeMinVersion) ||
      (req.isSafari && req.safariVersion.openOr(0) < SafariMinVersion)) false
    else true
  }

  private var test = false
  private def test_(b: Boolean) = test = b
}