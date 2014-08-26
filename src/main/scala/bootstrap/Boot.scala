package bootstrap.liftweb

import org.squeryl.Session
import org.squeryl.SessionFactory
import org.squeryl.adapters.PostgreSqlAdapter
import com.avyeyes.rest._
import com.avyeyes.util.AEConstants._
import com.avyeyes.util.AEHelpers._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.sitemap.LocPath.stringToLocPath
import net.liftweb.sitemap.Menu
import net.liftweb.sitemap.SiteMap
import net.liftweb.util.Vendor.valToVendor
import net.liftweb.util.Props


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable {
  def boot() = {
    logger.info("Lift booting")
    
    // where to search snippet
    LiftRules.addToPackages("com.avyeyes")
    
    LiftRules.setSiteMap(SiteMap(
      Menu.i("Home") / "index",
      Menu.i("Not Supported") / "notsupported"
    ))

    LiftRules.dispatch.prepend {
      case Req(path, _, _) if (path != List("notsupported") && !browserSupported(S.request)) => 
        () => {Full(RedirectResponse("/notsupported.html"))}
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
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    LiftRules.resourceNames = "text" :: "enum" :: "help" :: Nil
      
    initDb
  }
  
  ResourceServer.allow {
    case "js" :: _ => true
  	case "css" :: _ => true
  }

  private def initDb() = {
    logger.info("Initializing DB connection")
    
    import org.squeryl.SessionFactory
    import org.squeryl.Session
    import org.squeryl.adapters.PostgreSqlAdapter

    def jdbcConnectionString =  {
      new StringBuilder("jdbc:postgresql://")
       .append(getProp("db.host")).append(":")
       .append(getProp("db.port")).append("/")
       .append(getProp("db.name")).toString
    }
    
	Class.forName("org.postgresql.Driver")
	SessionFactory.concreteFactory = Some(()=>
	Session.create(java.sql.DriverManager.getConnection(jdbcConnectionString), new PostgreSqlAdapter))
  }
  
  private def browserSupported(reqBox: Box[Req]): Boolean = reqBox match {
      case Empty => false
      case Failure(_ ,_ ,_) => false
      case Full(request) => { 
        if (unboxBrowserVersion(request.chromeVersion) >= ChromeSupportedVersion
            || unboxBrowserVersion(request.firefoxVersion) >= FirefoxSupportedVersion
            || unboxBrowserVersion(request.safariVersion) >= SafariSupportedVersion
            || unboxBrowserVersion(request.ieVersion) >= IeSupportedVersion) {
          true
        } else {
          false
        }
      }
  }
  
  private def unboxBrowserVersion(versionBox: Box[Double]): Double = versionBox openOr 0.0
  private def unboxBrowserVersion(versionBox: Box[Int]): Int = versionBox openOr 0
}