package timetoteach.signup

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLButtonElement

object SignupJs {

  def loadJavascript(): Unit = {
//    userSignUpButton()
  }

  def userSignUpButton(): Unit = {
    val userSignupButton = dom.document.getElementById("userSignupButtonBtn").asInstanceOf[HTMLButtonElement]
    if (userSignupButton != null) {
      userSignupButton.addEventListener("click", (e: dom.Event) => {
        userSignupButton.disabled = true
      })
    }
  }
}
