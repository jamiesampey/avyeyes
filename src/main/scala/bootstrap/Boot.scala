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
    
    LiftRules.addToPackages("com.avyeyes")
    
    LiftRules.setSiteMap(SiteMap(
      Menu.i("Home") / "index",
      Menu.i("Browser Not Supported") / "whawha"
    ))

    // external ID URL extraction
    LiftRules.statelessRewrite.prepend {
      case RewriteRequest(ParsePath(extId :: Nil, "", _, false), GetRequest, _) => 
        RewriteResponse(ParsePath("index" :: Nil, "", true, true), Map(ExtIdUrlParam -> extId))
    }
    
    // browser not supported redirect
    LiftRules.statelessDispatch.prepend {
      case req @ Req(path, _, _) if (path != List("whawha") && !browserSupported(req)) => 
        () => {Full(RedirectResponse("/whawha.html"))}
    }
    
    // setup stateless REST endpoints
    LiftRules.statelessDispatch.append(ImageUpload)
    LiftRules.statelessDispatch.append(ImageServe)
    LiftRules.statelessDispatch.append(ExtIdVendor)
    LiftRules.statelessDispatch.append(AvyDetails)
    
    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    LiftRules.maxMimeFileSize = MaxImageSize
    LiftRules.maxMimeSize = MaxImageSize
    LiftRules.resourceNames = "text" :: "enum" :: "help" :: Nil
  }
  
  ResourceServer.allow {
    case "js" :: _ => true
  	case "css" :: _ => true
  }
  
  private def browserSupported(req: Req): Boolean = (
    unboxedBrowserVersion(req.chromeVersion) >= ChromeVersion
    || unboxedBrowserVersion(req.firefoxVersion) >= FirefoxVersion
    || unboxedBrowserVersion(req.safariVersion) >= SafariVersion
    || unboxedBrowserVersion(req.ieVersion) >= IeVersion)
  
  private def unboxedBrowserVersion(versionBox: Box[Double]): Double = versionBox openOr 0.0
  private def unboxedBrowserVersion(versionBox: Box[Int]): Int = versionBox openOr 0
}