package com.avyeyes.data

import com.avyeyes.model.Avalanche
import com.avyeyes.util.UserSession

import scala.collection.concurrent.TrieMap

class TrieMapDao(diskDao: DiskDao, user: UserSession) extends InMemoryDao {
  implicit val userSession: UserSession = user

  val avalancheMap: TrieMap[String, Avalanche] = new TrieMap
  avalancheMap ++= diskDao.getAllAvalanches.map(a => (a.extId, a))


  def countAvalanches(viewableOpt: Option[Boolean]): Int = viewableOpt match {
    case Some(viewable) => avalancheMap.filter(_._2.viewable == viewable).size
    case None => avalancheMap.size
  }

  def getAvalanche(extId: String) = avalancheMap.get(extId)

  def getAvalanches(query: AvalancheQuery) = {
    val matches = avalancheMap.values.filter(query.toPredicate).toList

    //TODO: handle order and pagination

    matches
  }

  def getAvalanches(query: AdminAvalancheQuery): (List[Avalanche], Int, Int) = ???

}
