package com.avyeyes.data

import com.avyeyes.model.Avalanche

trait InMemoryDao extends AuthorizableDao {
  def countAvalanches(viewable: Option[Boolean]): Int

  def getAvalanches(query: AvalancheQuery): List[Avalanche]

  def getAvalanches(query: AdminAvalancheQuery): (List[Avalanche], Int, Int)
}