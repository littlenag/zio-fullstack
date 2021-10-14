package ziofullstack.frontend.pages.secure

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import ziofullstack.frontend.components.InfoPanel

/**
  * Base page for the application.
  */
object HelpPage {

  private val component = ScalaComponent.static("HelpPage") {
    InfoPanel("Help")(
      <.div(
        // TODO provide links to docs, other stuff
        "Helpful resources on building SPAs with ZIO are on their way."
      )
    )
  }

  def apply() = component()
}
