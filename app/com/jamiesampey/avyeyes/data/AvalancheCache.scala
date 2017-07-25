package com.jamiesampey.avyeyes.data

import javax.inject.{Inject, Singleton}

import com.jamiesampey.avyeyes.model.Avalanche

import scala.collection.concurrent.{TrieMap, Map => CMap}

@Singleton
class AvalancheCache @Inject()() {
  private[data] val avalancheMap: CMap[String, Avalanche] = new TrieMap[String, Avalanche]()
}
