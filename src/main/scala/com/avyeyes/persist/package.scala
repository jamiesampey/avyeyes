import java.sql.Timestamp

import com.avyeyes.model.enums.Aspect.Aspect
import com.avyeyes.model.enums.AvalancheInterface.AvalancheInterface
import com.avyeyes.model.enums.AvalancheTrigger.AvalancheTrigger
import com.avyeyes.model.enums.AvalancheType.AvalancheType
import com.avyeyes.model.enums.ExperienceLevel.ExperienceLevel
import com.avyeyes.model.enums.ModeOfTravel.ModeOfTravel
import com.avyeyes.model.enums.Precipitation.Precipitation
import com.avyeyes.model.enums.SkyCoverage.SkyCoverage
import com.avyeyes.model.enums._
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

package object persist {

  object SlickColumnMappers {
    implicit def dateTimeMapper = MappedColumnType.base[DateTime, Timestamp](
      dt => new java.sql.Timestamp(dt.getMillis),
      ts => new DateTime(ts)
    )

    implicit def AspectMapper = MappedColumnType.base[Aspect, String](
      enum => enum.toString,
      str => Aspect.withName(str)
    )

    implicit def AvalancheInterfaceMapper = MappedColumnType.base[AvalancheInterface, String](
      enum => enum.toString,
      str => AvalancheInterface.withName(str)
    )

    implicit def AvalancheTriggerMapper = MappedColumnType.base[AvalancheTrigger, String](
      enum => enum.toString,
      str => AvalancheTrigger.withName(str)
    )

    implicit def AvalancheTypeMapper = MappedColumnType.base[AvalancheType, String](
      enum => enum.toString,
      str => AvalancheType.withName(str)
    )

    implicit def ExperienceLevelMapper = MappedColumnType.base[ExperienceLevel, String](
      enum => enum.toString,
      str => ExperienceLevel.withName(str)
    )

    implicit def ModeOfTravelMapper = MappedColumnType.base[ModeOfTravel, String](
      enum => enum.toString,
      str => ModeOfTravel.withName(str)
    )

    implicit def PrecipitationMapper = MappedColumnType.base[Precipitation, String](
      enum => enum.toString,
      str => Precipitation.withName(str)
    )

    implicit def SkyCoverageMapper = MappedColumnType.base[SkyCoverage, String](
      enum => enum.toString,
      str => SkyCoverage.withName(str)
    )
  }

}