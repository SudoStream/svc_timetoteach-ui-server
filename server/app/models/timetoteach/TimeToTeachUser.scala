package models.timetoteach

case class TimeToTeachUser(
                            firstName: String,
                            familyName: String,
                            emailAddress: String,
                            picture: String,
                            socialNetworkName: String,
                            socialNetworkUserId: String
                          )

//                            ,
//                            school: School
//                          )
//
//case class School(
//                   name: String,
//                   address: String,
//                   postCode: String,
//                   telephone: String,
//                   localAuthority: String,
//                   country: String
//                 )
