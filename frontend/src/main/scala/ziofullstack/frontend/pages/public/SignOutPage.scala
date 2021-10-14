package ziofullstack.frontend.pages.public

import diode.Dispatcher
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import ziofullstack.frontend.AppRouter.{AppPage, SignInPg}
import ziofullstack.frontend.model.Actions.SignOut

import scala.language.existentials

object SignOutPage {

  case class Props(router: RouterCtl[AppPage], dispatcher: Dispatcher)

  // create the React component for Home page
  private val component = ScalaComponent.builder[Props]("SignOut")
      .renderP { (_, props) =>
        // TODO fix with another action?
        val cb = Callback(props.dispatcher(SignOut)) >> props.router.set(SignInPg)
        cb.async.unsafeToFuture()
        <.div()
      }
      .build

  def apply(router: RouterCtl[AppPage], dispatcher: Dispatcher) =
    component(Props(router, dispatcher))
}
