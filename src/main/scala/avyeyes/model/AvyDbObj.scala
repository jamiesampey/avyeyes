package avyeyes.model

import java.sql.Timestamp
import org.squeryl.KeyedEntity

abstract class AvyDbObj extends KeyedEntity[Long] {
	val id: Long = 0
	val createTime = new Timestamp(System.currentTimeMillis)
}