package models.facebook

case class FacebookUser(
                         userFacebookId: String,
                         userFacebookEmail: String,
                         userPictureUri: String,
                         userFacebookGivenName: String,
                         userFacebookFamilyName: String
                       )
