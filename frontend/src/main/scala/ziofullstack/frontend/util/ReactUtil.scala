package ziofullstack.frontend.util

import diode._
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.vdom.TagMod

import scala.scalajs.js

object ReactUtil {

  final implicit def adapt(cb: Callback): js.UndefOr[() => Callback] = { () =>
    cb
  }

  final def When(condition: Boolean)(children: TagMod*): TagMod =
    if (condition) TagMod(children: _*) else TagMod.empty

  final def Unless(condition: Boolean)(children: TagMod*): TagMod =
    When(!condition)(children: _*)

  trait DispatcherCB extends Dispatcher {
    def dispatchCB[A: ActionType](action: A): Callback = Callback(dispatch(action))
  }
}