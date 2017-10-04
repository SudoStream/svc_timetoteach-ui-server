package models.timetoteach

case class School(
                   id: String,
                   name: String,
                   address: String,
                   postCode: String,
                   telephone: String,
                   localAuthority: LocalAuthority,
                   country: Country
                 )

case class Country(value: String)

case class LocalAuthority(value: String)
