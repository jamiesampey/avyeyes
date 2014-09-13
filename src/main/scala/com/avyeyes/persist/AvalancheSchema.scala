package com.avyeyes.persist

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema

import com.avyeyes.model._

object AvalancheSchema extends Schema {
  val avalanches = table[Avalanche]("avalanche")
  val avalancheImages = table[AvalancheImg]("avalanche_img")
  val avalancheImageDropbox = table[AvalancheImg]("avalanche_img_dropbox")
  
  on(avalanches)(a => declare(
    a.id is(primaryKey, autoIncremented),
    a.extId is(unique, indexed)
  ))
  
  on(avalancheImages)(img => declare(
      img.id is(primaryKey, autoIncremented),
      img.avyExtId is(indexed)
  ))
  
  on(avalancheImageDropbox)(img => declare(
      img.id is(primaryKey, autoIncremented)
  ))
}