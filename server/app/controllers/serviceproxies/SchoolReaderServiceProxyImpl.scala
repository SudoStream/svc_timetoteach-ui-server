package controllers.serviceproxies

import com.google.inject.Singleton
import models.timetoteach.{Country, LocalAuthority, School}

@Singleton
class SchoolReaderServiceProxyImpl {

  def getAllSchoolsAsSeq(): Seq[School] = {
    val stNiniansStirling = School(
      name = "Some School",
      address = "Torbrex Rd, Stirling",
      postCode = "FK7 9HN",
      telephone = "01786 237975",
      localAuthority = LocalAuthority("STIRLING"),
      country = Country("SCOTLAND")
    )

    val allansStirling = School(
      name = "Allan's Primary School",
      address = "Spittal Street, Stirling",
      postCode = "FK8 1DU",
      telephone = "01786 474757",
      localAuthority = LocalAuthority("STIRLING"),
      country = Country("SCOTLAND")
    )

    val riversideStirling = School(
      name = "Riverside Primary School",
      address = "Forrest Road, Stirling",
      postCode = "FK8 1UJ",
      telephone = "01786 474128",
      localAuthority = LocalAuthority("STIRLING"),
      country = Country("SCOTLAND")
    )

    val aStirlingSchool = School(
      name = "St. Ninians Primary School",
      address = "123 some street",
      postCode = "AB1 CD2",
      telephone = "0123456789",
      localAuthority = LocalAuthority("STIRLING"),
      country = Country("SCOTLAND")
    )

    val aGlasgowSchool = School(
      name = "Another School",
      address = "123 another street",
      postCode = "HS2 PL2",
      telephone = "11131517191",
      localAuthority = LocalAuthority("GLASGOW"),
      country = Country("SCOTLAND")
    )

    val aNewcastleSchool = School(
      name = "One More School",
      address = "123 one more street",
      postCode = "CS2 KP2",
      telephone = "143546317191",
      localAuthority = LocalAuthority("NEWCASTLE"),
      country = Country("ENGLAND")
    )

    List(stNiniansStirling, riversideStirling, allansStirling, aGlasgowSchool, aStirlingSchool, aNewcastleSchool)
  }

}
