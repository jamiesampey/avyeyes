//package com.avyeyes.controllers
//
//import com.avyeyes.data.AdminAvalancheQuery
//import play.api.mvc.QueryStringBindable
//
//object AdminQueryBindable extends QueryStringBindable[AdminAvalancheQuery] {
//    def bind(key: String, params: Map[String, Seq[String]]) = {
//      for {
//        slat <- params.get("slat")
//        slon <- params.get("slon")
//        elat <- params.get("elat")
//        elon <- params.get("elon")
//      } yield {
//        try {
//          Right(BoundingBox(Location(slat.toDouble, slon.toDouble), Location(elat.toDouble, elon.toDouble)))
//        } catch {
//          case e: Exception => Left(e.getMessage)
//        }
//      }
//    }
//
//    def unbind(key: String, v: BoundingBox) = {
//      s"slat=${v.start.lat}&slon=${v.start.lon}&elat=${v.end.lat}&elon=${v.end.lon}"
//    }
//}
