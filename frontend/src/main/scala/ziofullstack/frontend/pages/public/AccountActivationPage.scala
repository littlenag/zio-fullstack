package ziofullstack.frontend.pages.public

import io.github.littlenag.scalajs.components.`react-bootstrap`.{Card, CardBody, CardTitle}
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import scalacss.internal.mutable.GlobalRegistry
import ziofullstack.frontend.AppRouter.{AppPage, RegisterPg, SignInPg}
import ziofullstack.frontend.css.CssSettings._
import ziofullstack.frontend.services.BackendClient

import scala.concurrent.ExecutionContext
import scala.util.Try

/**
  * Page rendered when the user clicks their activation link. Takes a token, sends it to the server, and then
  * presents options based on the servers response.
  */
object AccountActivationPage {

  val logger = logstage.IzLogger()

  case class Props(router: RouterCtl[AppPage], token: String)

  case class State(isValidToken: Option[Boolean], submitted: Boolean)

  import scalacss.ScalaCssReact._

  GlobalRegistry.register(Style)

  object Style extends StyleSheet.Inline {
    import dsl._

    val outerDiv = style(
      textAlign.center,
      alignItems.flexStart,
      display.flex,
      flexDirection.column
    )

    val innerDiv = style(
      textAlign.left,
      //fontSize(20.px),
      minHeight(450.px),
      width(400.px),
      alignItems.flexStart,
      float.none,
      margin(0.px, auto)
    )
  }

  class Backend($ : BackendScope[Props, State]) {
    def render(p: Props, s: State) = {
      <.div(
        Style.outerDiv,
        <.div(
          Style.innerDiv,
          s.isValidToken match {
            case Some(true) =>
              Card()(
                CardBody()(
                  CardTitle("Your account has been activated."),
                  <.span(p.router.link(SignInPg)("Sign in."))
                )
              )
            case Some(false) =>
              Card()(
                CardBody()(
                  CardTitle("The token is not valid or has expired."),
                  "To generate a new activation token: first sign in, you will then be prompted with an option to re-send your activation email.",
                  <.br,
                  <.br,
                  "Please note that accounts not activated within 30 days are automatically removed.",
                  <.br,
                  <.br,
                  <.span(p.router.link(SignInPg)("Sign in.")),
                  <.br,
                  <.span(p.router.link(RegisterPg)("Create an account.")),
                )
              )
            case None =>
              Card()(CardBody()(s"Verifying Token"))
          }
        )
      )
    }
  }

  import ExecutionContext.Implicits.global

  val component = ScalaComponent
    .builder[Props]("AccountActivation")
    // create and store the connect proxy in state for later use
    .initialState(State(None, false))
    .renderBackend[Backend]
    .componentDidMount { $ =>
      // This should be a callback, but oh well.
      Callback.future(
        BackendClient
          .publicApi
          .activateAccount($.props.token)
          .transform { t =>
            t.fold(ex => logger("ex" -> ex).error("Failed."), _ => ())
            Try($.modState(_.copy(isValidToken = Some(t.isSuccess))))
          }
      )
    }
    .build

  // create the React component for Dashboard
  def apply(router: RouterCtl[AppPage], token: String) =
    component(Props(router, token))
}
