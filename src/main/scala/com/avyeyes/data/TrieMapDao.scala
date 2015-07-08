package com.avyeyes.data

import com.avyeyes.model.Avalanche
import com.avyeyes.util.UserSession

class TrieMapDao(implicit userSession: UserSession) extends InMemoryDao {
  def countAvalanches(viewable: Option[Boolean]): Int = ???

  def getAvalanches(query: AvalancheQuery): List[Avalanche] = ???

  def getAvalanches(query: AdminAvalancheQuery): (List[Avalanche], Int, Int) = ???
}
