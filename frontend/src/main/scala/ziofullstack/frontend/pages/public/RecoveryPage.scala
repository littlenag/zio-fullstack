package ziofullstack.frontend.pages.public

import diode.Dispatcher
import io.github.littlenag.scalajs.components.`react-bootstrap`.{Button, Card, CardBody}
import japgolly.scalajs.react.extra.{EventListener, OnUnmount}
import japgolly.scalajs.react.{Callback, _}
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^.{^, _}
import org.scalajs.dom
import scalacss.internal.mutable.GlobalRegistry
import ziofullstack.frontend.AppRouter.AppPage
import ziofullstack.frontend.css.CssSettings._
import ziofullstack.frontend.model.Actions.PasswordRecovery
import ziofullstack.frontend.util.Validation

object RecoveryPage {

  case class Props(router: RouterCtl[AppPage], dispatcher: Dispatcher)

  case class State(email:String,submitted:Boolean)

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

    val links = style(
      margin(17.px)
    )
  }

  class Backend($: ScalaComponent.BackendScope[Props, State]) extends OnUnmount {
    def emailForm(p: Props, s: State) = {
      <.div(
        <.div(//bss.formGroup,
          <.input.text(//bss.formControl,
            ^.id := "email",
            ^.value := s.email,
            ^.placeholder := "Email",
            ^.onChange ==> {ev: ReactEventFromInput => val text = ev.target.value; $.modState(_.copy(email = text))}
          )
        ),
        Button(
          variant = "primary",
          disabled = !isValidSubmitState(s),
          onClick = {() => $.modState(_.copy(submitted = true)) >> Callback(p.dispatcher(PasswordRecovery(s.email)))}
        )("Send Email")
      )
    }

    def render(p: Props, s: State) = {
      <.div(
        <.div(Style.outerDiv,
          <.div(Style.innerDiv,
            Card()(
              if (!s.submitted) {
                CardBody()(emailForm(p,s))
              } else {
                CardBody()(<.div("An email with a recovery link should arrive in the next few minutes."))
              }
            )
          )
        )
      )
    }

    def isValidSubmitState(s:State): Boolean = s.email.nonEmpty && Validation.isValidEmail(s.email) && !s.submitted

    def keydownEvent(e: dom.KeyboardEvent): Callback = {
      // Hook so that we try to sign in if the enter key is pressed
      for {
        state <- $.state
        props <- $.props
        _ <-
          if (e.key == "Enter" && isValidSubmitState(state)) {
            $.modState(_.copy(submitted = true)) >> Callback(props.dispatcher(PasswordRecovery(state.email)))
          } else {
            Callback.empty
          }
      } yield ()
    }
  }

  // create the React component for Dashboard

  def apply(router: RouterCtl[AppPage], dispatcher: Dispatcher) = {
    val component = ScalaComponent.builder[Props]("PasswordRecovery")
      // create and store the connect proxy in state for later use
      .initialState(State("",false))
      .renderBackend[Backend]
      .configure(EventListener[dom.KeyboardEvent].install("keydown", _.backend.keydownEvent))
      .build

    component(Props(router,dispatcher))
  }
}
