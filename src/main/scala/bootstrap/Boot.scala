package bootstrap.liftweb

import net.liftweb.http.{Html5Properties, LiftRules, Req, ResourceServer}
import net.liftweb.sitemap.{Menu, SiteMap}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    // where to search snippet
    LiftRules.addToPackages("avyeyes")
    
    // Build SiteMap
    def sitemap(): SiteMap = SiteMap(
      Menu.i("Home") / "index"
    )

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))
      
    initDb
  }
  
  ResourceServer.allow {
    case "js" :: _ => true
  	case "css" :: _ => true
  }

  def initDb = {
    import org.squeryl.SessionFactory
    import org.squeryl.Session
    import org.squeryl.adapters.PostgreSqlAdapter

	Class.forName("org.postgresql.Driver")
	SessionFactory.concreteFactory = Some(()=>
	Session.create(
		java.sql.DriverManager.getConnection("jdbc:postgresql://localhost:5432/avyeyes_db"),
		new PostgreSqlAdapter))
			
	import org.squeryl.PrimitiveTypeMode._
	import avyeyes.model.AvalancheDb
	transaction {
      AvalancheDb.printDdl
    }
  }
}