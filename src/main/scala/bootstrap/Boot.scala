package bootstrap.liftweb

import com.avyeyes.rest._
import com.avyeyes.util.AEConstants._
import com.avyeyes.util.AEHelpers._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.sitemap.LocPath.stringToLocPath
import net.liftweb.sitemap.Menu
import net.liftweb.sitemap.SiteMap
import net.liftweb.util.Vendor.valToVendor
import org.squeryl.SessionFactory
import org.squeryl.Session
import org.squeryl.adapters.PostgreSqlAdapter
import net.liftweb.sitemap.Loc
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
  def boot() = {
    logger.info("LIFT BOOT")
    
    LiftRules.addToPackages("com.avyeyes")
    
    val contextPaths = IndexPath :: BrowserNotSupportedPath :: LoginPath :: Nil
    val appMenus: List[Menu] = Menu(Loc("home", IndexPath :: Nil, "Home")) :: 
      Menu(Loc("browserNotSupported", BrowserNotSupportedPath :: Nil, "Browser Not Supported")) :: 
      Menu(Loc("logIn", LoginPath :: Nil, "Log In")) :: Nil
    val menus = appMenus ::: Omniauth.sitemap
    
    LiftRules.setSiteMap(SiteMap(menus:_*))
        
    // external ID URL extraction
    LiftRules.statelessRewrite.prepend {
      case RewriteRequest(ParsePath(extId :: Nil, "", _, false), GetRequest, _) if !contextPaths.contains(extId) => 
        RewriteResponse(ParsePath(IndexPath :: Nil, "", true, true), Map(ExtIdUrlParam -> extId))
    }
    
    // browser not supported redirect
    LiftRules.statelessDispatch.prepend {
      case req @ Req(path, _, _) if (path != List(BrowserNotSupportedPath) && !browserSupported(req)) => 
        () => {Full(RedirectResponse(BrowserNotSupportedPath))}
    }
    
    // setup REST endpoints
    LiftRules.dispatch.append(AvyDetails)
    LiftRules.dispatch.append(ImageServe)
    LiftRules.dispatch.append(OmniAuthCallback)
    LiftRules.statelessDispatch.append(ImageUpload)
    LiftRules.statelessDispatch.append(ExtIdVendor)

    
    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    LiftRules.maxMimeFileSize = MaxImageSize
    LiftRules.maxMimeSize = MaxImageSize
    LiftRules.resourceNames = "text" :: "enum" :: "help" :: Nil
    
    Omniauth.init // grabs omniauth.* settings from props file
    
    if (!test) initPostgresqlSession
  }
  
  ResourceServer.allow {
    case "js" :: _ => true
  	case "css" :: _ => true
  }
  
  lazy val jdbcConnectionString = new StringBuilder("jdbc:postgresql://")
    .append(getProp("db.host")).append(":")
    .append(getProp("db.port")).append("/")
    .append(getProp("db.name")).toString
 
  def initPostgresqlSession() = {
    if (SessionFactory.concreteFactory.isEmpty) {
      logger.info("Initializing Postgresql database session")
      Class.forName("org.postgresql.Driver")
      SessionFactory.concreteFactory = Some(()=>
        Session.create(java.sql.DriverManager.getConnection(jdbcConnectionString), new PostgreSqlAdapter))
    }
  }
    
  private def browserSupported(req: Req): Boolean = (
    unboxedBrowserVersion(req.chromeVersion) >= ChromeVersion
    || unboxedBrowserVersion(req.firefoxVersion) >= FirefoxVersion
    || unboxedBrowserVersion(req.safariVersion) >= SafariVersion
    || unboxedBrowserVersion(req.ieVersion) >= IeVersion)
  
  private def unboxedBrowserVersion(versionBox: Box[Double]): Double = versionBox openOr 0.0
  private def unboxedBrowserVersion(versionBox: Box[Int]): Int = versionBox openOr 0
  
  private var test = false
  private def test_(b: Boolean) = test = b
}