package com.avyeyes

import com.avyeyes.model.Avalanche

import scala.collection.concurrent.{TrieMap, Map => CMap}

package object data {
  type AvalancheMap = CMap[String, Avalanche]

  val AllAvalanchesMap: AvalancheMap = new TrieMap[String, Avalanche]()
}
