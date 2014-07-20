package bootstrap.liftweb

import net.liftweb.http._
import net.liftweb.sitemap.{Menu, SiteMap}
import net.liftweb.common.Full
import com.avyeyes.util.AEConstants._
import com.avyeyes.rest._


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    // where to search snippet
    LiftRules.addToPackages("com.avyeyes")
    
    LiftRules.statelessRewrite.prepend {
        case RewriteRequest(ParsePath(extId :: Nil, "", _, false), GetRequest, _) => 
            RewriteResponse(ParsePath("index" :: Nil, "", true, true), Map(EXT_ID_URL_PARAM -> extId))
    }

    LiftRules.setSiteMap(SiteMap(
      Menu.i("Home") / "index"
    ))

    LiftRules.maxMimeFileSize = MAX_IMAGE_SIZE
    LiftRules.maxMimeSize = MAX_IMAGE_SIZE
    
    LiftRules.statelessDispatchTable.append(ImageUpload)
    LiftRules.statelessDispatchTable.append(ImageServe)
    LiftRules.statelessDispatchTable.append(ExtIdVendor)
    
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

  private def initDb = {
    import org.squeryl.SessionFactory
    import org.squeryl.Session
    import org.squeryl.adapters.PostgreSqlAdapter

	Class.forName("org.postgresql.Driver")
	SessionFactory.concreteFactory = Some(()=>
	Session.create(java.sql.DriverManager.getConnection(JDBC_CONNECT_STR), new PostgreSqlAdapter))
  }
}