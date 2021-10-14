package ziofullstack.frontend.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.internal.mutable.GlobalRegistry

object Footer {

  import ziofullstack.frontend.css.CssSettings._
  //import scalacss.ScalaCssReact._

  object Style extends StyleSheet.Inline {

    import dsl._

    val footerDiv = style(
      position.absolute,
      bottom(0.px),
      width(100.%%),
      height(30.px) /* Height of the footer */
    )
  }

  GlobalRegistry.register(Style)

  val component = ScalaComponent.builder.static("Footer")(
    EmptyVdom
  ).build

  def apply() = component()
}