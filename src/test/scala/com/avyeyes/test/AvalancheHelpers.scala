package com.avyeyes.test

import com.avyeyes.data.CachedDao
import com.avyeyes.model._
import com.avyeyes.model.enums._
import org.joda.time.DateTime

trait AvalancheHelpers {

  def insertTestAvalanche(dao: CachedDao, a: Avalanche) = dao.insertAvalanche(a, "thomas.jefferson@gmail.com")
  def insertTestAvalanche(dao: CachedDao, a: Avalanche, submitterEmail: String) = dao.insertAvalanche(a, submitterEmail)

  class TestAvalanche extends Avalanche(
    extId = "4fj945fs",
    viewable = true,
    location = Coordinate(-105.88556584, 39.76798644, 2849),
    submitterExp = ExperienceLevel.A0,
    areaName = "Just some place on the map",
    avyDate = DateTime.now,
    sky = SkyCoverage.U,
    precip = Precipitation.U,
    aspect = Aspect.N,
    angle = 45,
    avyType = AvalancheType.U,
    avyTrigger = AvalancheTrigger.U,
    avyInterface = AvalancheInterface.U,
    rSize = 0.0,
    dSize = 0.0,
    caught = 0,
    partiallyBuried = 0,
    fullyBuried = 0,
    injured = 0,
    killed = 0,
    modeOfTravel = ModeOfTravel.U,
    comments = "Just some comments about this avalanche",
    perimeter = Seq.empty,
    submitterId = -1
  )
}