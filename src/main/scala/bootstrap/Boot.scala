package bootstrap.liftweb

import net.liftweb.http.{Html5Properties, LiftRules, Req, ResourceServer}
import net.liftweb.sitemap.{Menu, SiteMap}
import avyeyes.model.Avalanche
import avyeyes.snippet.AvySearch
import avyeyes.util.AEConstants._
import net.liftweb.sitemap.Loc.EarlyResponse
import net.liftweb.common.Full
import net.liftweb.http.PlainTextResponse
import net.liftweb.http.PageName
import net.liftweb.http.RewriteRequest
import net.liftweb.http.ParsePath
import net.liftweb.http.GetRequest
import net.liftweb.http.RewriteResponse

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    // where to search snippet
    LiftRules.addToPackages("avyeyes")
    
    LiftRules.statelessRewrite.prepend {
        case RewriteRequest(ParsePath(extId :: Nil, _, _, false), GetRequest, _) => 
            RewriteResponse(ParsePath("index" :: Nil, "", true, true), Map(EXT_ID_URL_PARAM -> extId))
    }

    LiftRules.setSiteMap(SiteMap(
      Menu.i("Home") / "index"
    ))

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

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
	Session.create(
		java.sql.DriverManager.getConnection("jdbc:postgresql://localhost:5432/avyeyes_db"),
		new PostgreSqlAdapter))
  }
}