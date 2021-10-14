package ziofullstack.frontend.pages.public

import diode.Dispatcher
import io.github.littlenag.scalajs.components.`react-bootstrap`.{Button, Card, CardBody, CardHeader, CardTitle}
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.{EventListener, OnUnmount}
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^.{^, _}
import org.scalajs.dom
import scalacss.internal.mutable.{GlobalRegistry, StyleSheet}
import ziofullstack.frontend.AppRouter.{AppPage, RecoveryPg, RegisterPg, SignInPg}
import ziofullstack.frontend.css.CssSettings._
import ziofullstack.frontend.model.Actions.PasswordReset
import ziofullstack.frontend.services.BackendClient

import scala.concurrent.ExecutionContext
import scala.language.existentials
import scala.util.Try

object PasswordResetPage {

  case class Props(router: RouterCtl[AppPage], token: String, dispatcher: Dispatcher)

  case class State(password1: String,
                   password2: String,
                   isValidToken: Option[Boolean])

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

  class Backend($ : ScalaComponent.BackendScope[Props, State])
      extends OnUnmount {
    def passwordResetForm(p: Props, s: State) = {
      <.div(
        <.div(
          //bss.formGroup,
          <.label(^.`for` := "description", "Password"),
          <.input.text(
            //bss.formControl,
            ^.id := "password",
            ^.value := s.password1,
            ^.`type` := "password",
            ^.placeholder := "",
            ^.onChange ==> { ev: ReactEventFromInput =>
              val text = ev.target.value; $.modState(_.copy(password1 = text))
            }
          )
        ),
        <.div(
          //bss.formGroup,
          <.label(^.`for` := "description", "Confirm Password"),
          <.input.text(
            //bss.formControl,
            ^.id := "password2",
            ^.value := s.password2,
            ^.`type` := "password",
            ^.placeholder := "",
            ^.onChange ==> { ev: ReactEventFromInput =>
              val text = ev.target.value; $.modState(_.copy(password2 = text))
            }
          )
        ),
        Button(
          variant = "primary",
          disabled = !isValidSubmitState(s),
          onClick = () =>
            Callback(p.dispatcher(PasswordReset(p.token, s.password1))) >>
              p.router.set(SignInPg)
        )("Reset")
      )
    }

    def render(p: Props, s: State) = {
      <.div(
        Style.outerDiv,
        <.div(
          Style.innerDiv,
          s.isValidToken match {
            case Some(true) =>
              Card()(
                CardHeader()(s"Password Reset"),
                CardBody()(passwordResetForm(p, s))
              )
            case Some(false) =>
              Card()(
                CardBody()(
                  CardTitle()("The token is not valid or has expired."),
                  <.span(p.router.link(RecoveryPg)("Forgot your password?")),
                  <.br,
                  <.span(p.router.link(RegisterPg)("Create an account."))
                )
              )
            case None =>
              Card()(CardBody()(s"Verifying Token"))
          }
        )
      )
    }

    def isValidSubmitState(s: State): Boolean = s.password1 == s.password2

    def keydownEvent(e: dom.KeyboardEvent): Callback = {
      // Hook so that we try to sign in if the enter key is pressed
      for {
        state <- $.state
        props <- $.props
        _ <- if (e.key == "Enter" && isValidSubmitState(state)) {
          Callback(props.dispatcher(PasswordReset(props.token, state.password1))) >>
            props.router.set(SignInPg)
        } else {
          Callback.empty
        }
      } yield ()
    }
  }

  import ExecutionContext.Implicits.global

  val logger = logstage.IzLogger()

  val component = ScalaComponent
    .builder[Props]("PasswordReset")
    // create and store the connect proxy in state for later use
    .initialState(State("", "", None))
    .renderBackend[Backend]
    .componentDidMount { $ =>
      // This should be a callback, but oh well.
      Callback.future(
        BackendClient
          .publicApi
          .validateRecoveryToken($.props.token)
          .transform { t =>
            t.fold(ex => logger("ex" -> ex).error("Failed."), _ => ())
            Try($.modState(_.copy(isValidToken = Some(t.isSuccess))))
          }
      )
    }
    .configure(
      EventListener[dom.KeyboardEvent]
        .install("keydown", _.backend.keydownEvent)
    )
    .build

  // create the React component for Dashboard
  def apply(router: RouterCtl[AppPage], token: String, dispatcher: Dispatcher) =
    component(Props(router, token, dispatcher))
}
