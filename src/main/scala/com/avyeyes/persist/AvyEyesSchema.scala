package com.avyeyes.persist

import com.avyeyes.model._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.CompositeKey2
import org.squeryl.{KeyedEntity, Schema}

object AvyEyesSchema extends Schema {

  val avalanches = table[Avalanche]("avalanche")
  val avalancheImages = table[AvalancheImage]("avalanche_image")
  val users = table[User]("app_user")
  val userRoles = table[UserRole]("app_user_role")

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
    user.id is(primaryKey, autoIncremented),
    user.email is(unique, indexed)
  ))

  on(userRoles)(role => declare(
    role.id is(primaryKey, autoIncremented)
  ))

  val userToAvalanches = oneToManyRelation(users, avalanches).via((u,a) => u.id === a.submitterId)

  val userRoleAssignments = manyToManyRelation(users, userRoles, "app_user_role_assignment")
    .via[UserRoleAssignment]((u,r,ur) => (ur.userId === u.id, r.id === ur.roleId))
}

class UserRoleAssignment(val userId: Long, val roleId: Long) extends KeyedEntity[CompositeKey2[Long, Long]] {
  def id = compositeKey(userId, roleId)
}
