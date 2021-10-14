package ziofullstack.frontend.util

import japgolly.scalajs.react.vdom.html_<^._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  *
  */
object FontAwesome {
  def apply(name: String): VdomNode = <.i(^.className := s"fas fa-$name")

  def solid(name: String): VdomNode = <.i(^.className := s"fas fa-$name")

  def fixedWidth(name: String): VdomNode =
    <.i(^.className := s"fas fa-fw fa-$name")

  @js.native
  @JSImport("@fortawesome/fontawesome-free/css/all.css", JSImport.Namespace)
  object CssImport extends js.Object
}
