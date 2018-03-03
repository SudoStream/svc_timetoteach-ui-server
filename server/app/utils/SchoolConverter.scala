package utils

import io.sudostream.timetoteach.messages.systemwide.model
import models.timetoteach.{Country, LocalAuthority, School}
import play.api.Logger


object SchoolConverter {

  private val logger: Logger.type = Logger

  def convertMessageSchoolToLocalSchool(schoolToConvert: io.sudostream.timetoteach.messages.systemwide.model.School):
  School = {
    School(
      id = schoolToConvert.id,
      name = schoolToConvert.name,
      address = schoolToConvert.address,
      postCode = schoolToConvert.postCode,
      telephone = schoolToConvert.telephone,
      localAuthority = convertLocalAuthorityToModelVersion(schoolToConvert.localAuthority),
      country = convertCountryToModelVersion(schoolToConvert.country)
    )
  }


  def convertLocalSchoolToMessageSchool(schoolToConvert: School):
  io.sudostream.timetoteach.messages.systemwide.model.School = {
    io.sudostream.timetoteach.messages.systemwide.model.School(
      id = schoolToConvert.id,
      name = schoolToConvert.name,
      address = schoolToConvert.address,
      postCode = schoolToConvert.postCode,
      telephone = schoolToConvert.telephone,
      localAuthority = convertLocalAuthorityToMessageVersion(schoolToConvert.localAuthority),
      country = convertCountryToMessageVersion(schoolToConvert.country)
    )
  }

  def convertLocalAuthorityToMessageVersion(localAuthority: LocalAuthority):
  io.sudostream.timetoteach.messages.systemwide.model.LocalAuthority = {
    localAuthority.value.toUpperCase match {
      case "ABERDEEN_CITY" => model.LocalAuthority.SCOTLAND__ABERDEEN_CITY
      case "ABERDEENSHIRE" => model.LocalAuthority.SCOTLAND__ABERDEENSHIRE
      case "ANGUS" => model.LocalAuthority.SCOTLAND__ANGUS
      case "ARGYLL_AND_BUTE" => model.LocalAuthority.SCOTLAND__ARGYLL_AND_BUTE
      case "COMHAIRLE_NAN_EILEAN_SIAR" => model.LocalAuthority.SCOTLAND__COMHAIRLE_NAN_EILEAN_SIAR
      case "CLACKMANNANSHIRE" => model.LocalAuthority.SCOTLAND__CLACKMANNANSHIRE
      case "DUMFRIES_AND_GALLOWAY" => model.LocalAuthority.SCOTLAND__DUMFRIES_AND_GALLOWAY
      case "DUNDEE_CITY" => model.LocalAuthority.SCOTLAND__DUNDEE_CITY
      case "EAST_AYRSHIRE" => model.LocalAuthority.SCOTLAND__EAST_AYRSHIRE
      case "EAST_DUMBARTONSHIRE" => model.LocalAuthority.SCOTLAND__EAST_DUMBARTONSHIRE
      case "EDINBURGH_CITY" => model.LocalAuthority.SCOTLAND__EDINBURGH_CITY
      case "EAST_LOTHIAN" => model.LocalAuthority.SCOTLAND__EAST_LOTHIAN
      case "EAST_RENFREWSHIRE" => model.LocalAuthority.SCOTLAND__EAST_RENFREWSHIRE
      case "FALKIRK" => model.LocalAuthority.SCOTLAND__FALKIRK
      case "FIFE" => model.LocalAuthority.SCOTLAND__FIFE
      case "GLASGOW" => model.LocalAuthority.SCOTLAND__GLASGOW
      case "HIGHLAND" => model.LocalAuthority.SCOTLAND__HIGHLAND
      case "INVERCLYDE" => model.LocalAuthority.SCOTLAND__INVERCLYDE
      case "MIDLOTHIAN" => model.LocalAuthority.SCOTLAND__MIDLOTHIAN
      case "MORAY" => model.LocalAuthority.SCOTLAND__MORAY
      case "NORTH_AYRSHIRE" => model.LocalAuthority.SCOTLAND__NORTH_AYRSHIRE
      case "NORTH_LANARKSHIRE" => model.LocalAuthority.SCOTLAND__NORTH_LANARKSHIRE
      case "ORKNEY" => model.LocalAuthority.SCOTLAND__ORKNEY
      case "PERTH_AND_KINROSS" => model.LocalAuthority.SCOTLAND__PERTH_AND_KINROSS
      case "RENFREWSHIRE" => model.LocalAuthority.SCOTLAND__RENFREWSHIRE
      case "SCOTTISH_BORDERS" => model.LocalAuthority.SCOTLAND__SCOTTISH_BORDERS
      case "SHETLAND_ISLANDS" => model.LocalAuthority.SCOTLAND__SHETLAND_ISLANDS
      case "SOUTH_AYRSHIRE" => model.LocalAuthority.SCOTLAND__SOUTH_AYRSHIRE
      case "SOUTH_LANARKSHIRE" => model.LocalAuthority.SCOTLAND__SOUTH_LANARKSHIRE
      case "STIRLING" => model.LocalAuthority.SCOTLAND__STIRLING
      case "WEST_DUMBARTONSHIRE" | "WEST_DUNBARTONSHIRE" => model.LocalAuthority.SCOTLAND__WEST_DUMBARTONSHIRE
      case "WEST_LOTHIAN" => model.LocalAuthority.SCOTLAND__WEST_LOTHIAN
      case "OTHER" => model.LocalAuthority.OTHER
      case unknown =>
        logger.warn(s"Local Authority is unknown - ${unknown}")
        model.LocalAuthority.UNKNOWN
    }
  }

  def convertLocalAuthorityToModelVersion(localAuthority: io.sudostream.timetoteach.messages.systemwide.model.LocalAuthority):
  LocalAuthority = {
    localAuthority match {
      case model.LocalAuthority.SCOTLAND__ABERDEEN_CITY => LocalAuthority("ABERDEEN_CITY")
      case model.LocalAuthority.SCOTLAND__ABERDEENSHIRE => LocalAuthority("ABERDEENSHIRE")
      case model.LocalAuthority.SCOTLAND__ANGUS => LocalAuthority("ANGUS")
      case model.LocalAuthority.SCOTLAND__ARGYLL_AND_BUTE => LocalAuthority("ARGYLL_AND_BUTE")
      case model.LocalAuthority.SCOTLAND__COMHAIRLE_NAN_EILEAN_SIAR => LocalAuthority("COMHAIRLE_NAN_EILEAN_SIAR")
      case model.LocalAuthority.SCOTLAND__CLACKMANNANSHIRE => LocalAuthority("CLACKMANNANSHIRE")
      case model.LocalAuthority.SCOTLAND__DUMFRIES_AND_GALLOWAY => LocalAuthority("DUMFRIES_AND_GALLOWAY")
      case model.LocalAuthority.SCOTLAND__DUNDEE_CITY => LocalAuthority("DUNDEE_CITY")
      case model.LocalAuthority.SCOTLAND__EAST_AYRSHIRE => LocalAuthority("EAST_AYRSHIRE")
      case model.LocalAuthority.SCOTLAND__EAST_DUMBARTONSHIRE => LocalAuthority("EAST_DUMBARTONSHIRE")
      case model.LocalAuthority.SCOTLAND__EDINBURGH_CITY => LocalAuthority("EDINBURGH_CITY")
      case model.LocalAuthority.SCOTLAND__EAST_LOTHIAN => LocalAuthority("EAST_LOTHIAN")
      case model.LocalAuthority.SCOTLAND__EAST_RENFREWSHIRE => LocalAuthority("EAST_RENFREWSHIRE")
      case model.LocalAuthority.SCOTLAND__FALKIRK => LocalAuthority("FALKIRK")
      case model.LocalAuthority.SCOTLAND__FIFE => LocalAuthority("FIFE")
      case model.LocalAuthority.SCOTLAND__GLASGOW => LocalAuthority("GLASGOW")
      case model.LocalAuthority.SCOTLAND__HIGHLAND => LocalAuthority("HIGHLAND")
      case model.LocalAuthority.SCOTLAND__INVERCLYDE => LocalAuthority("INVERCLYDE")
      case model.LocalAuthority.SCOTLAND__MIDLOTHIAN => LocalAuthority("MIDLOTHIAN")
      case model.LocalAuthority.SCOTLAND__MORAY => LocalAuthority("MORAY")
      case model.LocalAuthority.SCOTLAND__NORTH_AYRSHIRE => LocalAuthority("NORTH_AYRSHIRE")
      case model.LocalAuthority.SCOTLAND__NORTH_LANARKSHIRE => LocalAuthority("NORTH_LANARKSHIRE")
      case model.LocalAuthority.SCOTLAND__ORKNEY => LocalAuthority("ORKNEY")
      case model.LocalAuthority.SCOTLAND__PERTH_AND_KINROSS => LocalAuthority("PERTH_AND_KINROSS")
      case model.LocalAuthority.SCOTLAND__RENFREWSHIRE => LocalAuthority("RENFREWSHIRE")
      case model.LocalAuthority.SCOTLAND__SCOTTISH_BORDERS => LocalAuthority("SCOTTISH_BORDERS")
      case model.LocalAuthority.SCOTLAND__SHETLAND_ISLANDS => LocalAuthority("SHETLAND_ISLANDS")
      case model.LocalAuthority.SCOTLAND__SOUTH_AYRSHIRE => LocalAuthority("SOUTH_AYRSHIRE")
      case model.LocalAuthority.SCOTLAND__SOUTH_LANARKSHIRE => LocalAuthority("SOUTH_LANARKSHIRE")
      case model.LocalAuthority.SCOTLAND__STIRLING => LocalAuthority("STIRLING")
      case model.LocalAuthority.SCOTLAND__WEST_DUMBARTONSHIRE => LocalAuthority("WEST_DUNBARTONSHIRE")
      case model.LocalAuthority.SCOTLAND__WEST_LOTHIAN => LocalAuthority("WEST_LOTHIAN")
      case model.LocalAuthority.OTHER => LocalAuthority("OTHER")
      case _ => LocalAuthority("UNKNOWN")
    }
  }


  def convertCountryToMessageVersion(country: Country):
  io.sudostream.timetoteach.messages.systemwide.model.Country = {
    country.value.toUpperCase match {
      case "EIRE" => model.Country.EIRE
      case "ENGLAND" => model.Country.ENGLAND
      case "NORTHERN_IRELAND" => model.Country.NORTHERN_IRELAND
      case "SCOTLAND" => model.Country.SCOTLAND
      case "WALES" => model.Country.WALES
      case "OTHER" => model.Country.OTHER
      case _ => model.Country.UNKNOWN
    }
  }

  def convertCountryToModelVersion(country: io.sudostream.timetoteach.messages.systemwide.model.Country):
  Country = {
    country match {
      case model.Country.EIRE => Country("EIRE")
      case model.Country.ENGLAND => Country("ENGLAND")
      case model.Country.NORTHERN_IRELAND => Country("NORTHERN_IRELAND")
      case model.Country.SCOTLAND => Country("SCOTLAND")
      case model.Country.WALES => Country("WALES")
      case model.Country.OTHER => Country("OTHER")
      case _ => Country("UNKNOWN")
    }
  }

}

