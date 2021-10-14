package ziofullstack.frontend.css

import CssSettings._

object GlobalStyles extends StyleSheet.Inline {
  import dsl._

  style(
    unsafeRoot("body")(
      margin.`0`,
      padding.`0`,
      fontSize(14.px),
      fontFamily :=! "Roboto, sans-serif"
    )
  )

  val infoPanel = style(
    addClassName("container-fluid"),
    margin(10.px),
  )

  val fullPagePanel = style(
    addClassName("container-fluid"),
    margin(10.px),
    height(100.%%),
  )

  val colCentered = style(
    float.none,
    margin(0.px, auto)
  )
}
