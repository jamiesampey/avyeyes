package bootstrap.liftweb

import akka.actor.{ActorSystem, Props}
import com.avyeyes.persist.DatabaseMaintainer
import com.avyeyes.rest._
import com.avyeyes.util.Constants._
import com.avyeyes.util.Helpers._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.util.Vendor.valToVendor
import omniauth.Omniauth
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.{Session, SessionFactory}

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
  val actorSystem = ActorSystem()

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
    LiftRules.dispatch.append(OmniAuthCallback)
    LiftRules.dispatch.append(AvyDetails)
    LiftRules.dispatch.append(Images)
    LiftRules.statelessDispatch.append(ExtIdVendor)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    LiftRules.maxMimeFileSize = MaxImageSize
    LiftRules.maxMimeSize = MaxImageSize
    LiftRules.resourceNames = "text" :: "enum" :: "help" :: Nil
    
    Omniauth.init // grabs omniauth.* settings from props file
    
    if (!test) {
      initPostgresqlSession

      import actorSystem.dispatcher
      import scala.concurrent.duration._
      actorSystem.scheduler.schedule(
        initialDelay = getProp("db.maintenanceDelayMinutes").toInt minutes,
        interval = getProp("db.maintenanceIntervalHours").toInt hours,
        receiver = actorSystem.actorOf(Props(new DatabaseMaintainer)),
        message = DatabaseMaintainer.PerformMaintenance)
    }
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
    //org.squeryl.PrimitiveTypeMode.transaction { com.avyeyes.persist.AvyEyesSchema.printDdl }
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