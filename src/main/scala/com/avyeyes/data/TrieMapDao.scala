package com.avyeyes.data

import com.avyeyes.model.Avalanche
import com.avyeyes.util.UserSession

class TrieMapDao(user: UserSession) extends InMemoryDao {
  implicit val userSession: UserSession = user

  def countAvalanches(viewable: Option[Boolean]): Int = ???

  def getAvalanche(extId: String): Option[Avalanche] = ???

  def getAvalanches(query: AvalancheQuery): List[Avalanche] = ???

  def getAvalanches(query: AdminAvalancheQuery): (List[Avalanche], Int, Int) = ???
}
