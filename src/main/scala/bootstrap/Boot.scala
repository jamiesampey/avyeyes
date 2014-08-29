package bootstrap.liftweb

import com.avyeyes.rest._
import com.avyeyes.util.AEConstants._

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.sitemap.LocPath.stringToLocPath
import net.liftweb.sitemap.Menu
import net.liftweb.sitemap.SiteMap
import net.liftweb.util.Vendor.valToVendor

/**
 * Companion object for unit testing
 */
object Boot {
  def apply() = new Boot
}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable {
  def boot() = {
    logger.info("LIFT BOOT")
    
    // where to search snippet
    LiftRules.addToPackages("com.avyeyes")
    
    LiftRules.setSiteMap(SiteMap(
      Menu.i("Home") / "index",
      Menu.i("Browser Not Supported") / "whawha"
    ))

    LiftRules.dispatch.prepend {
      case Req(path, _, _) if (path != List("whawha") && !browserSupported(S.request)) => 
        () => {Full(RedirectResponse("/whawha.html"))}
    }
    
    LiftRules.statelessRewrite.prepend {
        case RewriteRequest(ParsePath(extId :: Nil, "", _, false), GetRequest, _) => 
            RewriteResponse(ParsePath("index" :: Nil, "", true, true), Map(ExtIdUrlParam -> extId))
    }
    
    LiftRules.maxMimeFileSize = MaxImageSize
    LiftRules.maxMimeSize = MaxImageSize
    
    // setup REST endpoints
    LiftRules.statelessDispatch.append(ImageUpload)
    LiftRules.statelessDispatch.append(ImageServe)
    LiftRules.statelessDispatch.append(ExtIdVendor)
    LiftRules.statelessDispatch.append(AvyDetails)
    
    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    LiftRules.resourceNames = "text" :: "enum" :: "help" :: Nil
  }
  
  ResourceServer.allow {
    case "js" :: _ => true
  	case "css" :: _ => true
  }
  
  private def browserSupported(reqBox: Box[Req]): Boolean = reqBox match {
      case Empty => false
      case Failure(_ ,_ ,_) => false
      case Full(request) => { 
        if (unboxedBrowserVersion(request.chromeVersion) >= ChromeVersion
            || unboxedBrowserVersion(request.firefoxVersion) >= FirefoxVersion
            || unboxedBrowserVersion(request.safariVersion) >= SafariVersion
            || unboxedBrowserVersion(request.ieVersion) >= IeVersion) {
          true
        } else {
          false
        }
      }
  }
  
  private def unboxedBrowserVersion(versionBox: Box[Double]): Double = versionBox openOr 0.0
  private def unboxedBrowserVersion(versionBox: Box[Int]): Int = versionBox openOr 0
}