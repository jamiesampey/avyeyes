package com.avyeyes.service

case class AvalancheSearchCriteria(
  northLimit: String, eastLimit: String, southLimit: String, westLimit: String, 
  fromDateStr: String, toDateStr: String, avyTypeStr: String, avyTriggerStr: String, 
  rSize: String, dSize: String, numCaught: String, numKilled: String) 