package com.avyeyes.test

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.json._
import net.liftweb.json.JsonAST._
import java.util.Date

trait LiftHelpers {
  implicit val formats = DefaultFormats
  
  def openLiftReqBox(box: Box[Req]): Req = box match {
    case Empty => null
    case Failure(_,_,_) => null
    case Full(req) => req
  }
  
  def openLiftRespBox(box: Box[LiftResponse]): LiftResponse = box match {
    case Empty => null
    case Failure(_,_,_) => null
    case Full(resp) => resp
  }
  
  def jsonResponseAsJValue(response: LiftResponse): JValue = {
    JsonParser.parse(response.asInstanceOf[JsonResponse].json.toJsCmd)
  }
  
  def extractJsonStringField(resp: LiftResponse, field: String):String = (jsonResponseAsJValue(resp) \\ field).extract[String]
  def extractJsonDoubleField(resp: LiftResponse, field: String): Double = (jsonResponseAsJValue(resp) \\ field).extract[Double]
  def extractJsonLongField(resp: LiftResponse, field: String): Long = (jsonResponseAsJValue(resp) \\ field).extract[Long]
  
  def addFileUploadToReq(orig: Req, fph: FileParamHolder): Req = {
    val fphParamCalcInfo = new ParamCalcInfo(Nil, null, fph :: Nil, Empty)
    new Req(orig.path, orig.contextPath, orig.requestType, orig.contentType, orig.request,
      orig.nanoStart, orig.nanoEnd, orig.stateless_?, () => fphParamCalcInfo, Map.empty)
  }
}