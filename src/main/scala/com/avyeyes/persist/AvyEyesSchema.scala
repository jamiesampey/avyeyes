package com.avyeyes.persist

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema

import com.avyeyes.model._

object AvyEyesSchema extends Schema {
  val avalanches = table[Avalanche]("avalanche")
  val avalancheImages = table[AvalancheImage]("avalanche_image")
  val users = table[User]("app_user")
  
  on(avalanches)(a => declare(
    a.id is(primaryKey, autoIncremented),
    a.extId is(unique, indexed)
  ))
  
  on(avalancheImages)(img => declare(
    img.id is(primaryKey, autoIncremented),
    img.avyExtId is(indexed),
    columns(img.avyExtId, img.filename) are(unique) 
  ))
  
  on(users)(user => declare(
    user.id is(primaryKey, autoIncremented)
  ))
}