package com.avyeyes

import com.avyeyes.model.Avalanche

import scala.collection.concurrent.{TrieMap, Map => CMap}

package object data {
  val AllAvalanchesMap: CMap[String, Avalanche] = new TrieMap()
}
