package avyeyes.model

import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._

import avyeyes.util.AEHelpers._

object AvalancheDb extends Schema {
	val avalanches = table[Avalanche]("avalanche")
	
	on(avalanches)(a => declare(
		a.id is(primaryKey, autoIncremented),
		a.extId is(unique, indexed)
	))
	
	def getAvalancheByExtId(extId: Option[String]): Option[Avalanche] = {
      if (isValidExtId(extId)) {
          transaction {
            avalanches.where(a => a.extId === extId).headOption
          }
      } else
        None
    }
}