package com.avyeyes.test

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.json._
import net.liftweb.json.JsonAST._

trait LiftHelpers {
  implicit val formats = DefaultFormats
  
  def openLiftReqBox(box: Box[Req]): Req = box match {
    case Empty => null
    case Failure(_,_,_) => null
    case Full(req) => req
  }
  
  def openLiftResponseBox(box: Box[LiftResponse]): LiftResponse = box match {
    case Empty => null
    case Failure(_,_,_) => null
    case Full(resp) => resp
  }
  
  def responseAsJValue(box: Box[LiftResponse]): JValue = {
    val response = openLiftResponseBox(box)
    JsonParser.parse(response.asInstanceOf[JsonResponse].json.toJsCmd)
  }
  
  def extractJsonFieldValue(json: JValue, field: String) = (json \\ field).extract[String]
}