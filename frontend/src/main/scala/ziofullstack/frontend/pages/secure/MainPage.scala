package ziofullstack.frontend.pages.secure

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import ziofullstack.frontend.components.InfoPanel

/**
  * Main landing page after autenticating for the application.
  */
object MainPage {

  private val component = ScalaComponent.static("MainPage") {
    InfoPanel("App")(
      <.div("Welcome to the ZIO Fullstack app.")
    )
  }

  def apply() = component()
}
