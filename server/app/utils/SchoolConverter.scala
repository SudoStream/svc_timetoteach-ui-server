package utils

import io.sudostream.timetoteach.messages.systemwide.model
import models.timetoteach.{Country, LocalAuthority, School}


object SchoolConverter {

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
      case "WEST_DUMBARTONSHIRE" => model.LocalAuthority.SCOTLAND__WEST_DUMBARTONSHIRE
      case "WEST_LOTHIAN" => model.LocalAuthority.SCOTLAND__WEST_LOTHIAN
      case "OTHER" => model.LocalAuthority.OTHER
      case _ => model.LocalAuthority.UNKNOWN
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
}

