package avyeyes.model

import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._

object AvalancheDb extends Schema {
	val avalanches = table[Avalanche]("avalanche")
	
	on(avalanches)(a => declare(
		a.id is(primaryKey, autoIncremented),
		a.extId is(unique, indexed)
	))
	
}