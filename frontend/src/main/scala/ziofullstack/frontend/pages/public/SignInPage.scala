package ziofullstack.frontend.pages.public

import diode.Dispatcher
import diode.react.ReactPot._
import diode.data._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.{EventListener, OnUnmount}
import diode.data.PotState.PotEmpty
import io.github.littlenag.scalajs.components.`react-bootstrap`.{Button, Card, CardBody, CardHeader}
import org.scalajs.dom
import scalacss.internal.mutable.GlobalRegistry
import scalacss.ScalaCssReact._
import ziofullstack.frontend.AppRouter._
import ziofullstack.frontend.css.CssSettings._
import ziofullstack.frontend.model.Actions.SignIn
import ziofullstack.frontend.util.Validation
import ziofullstack.shared.domain.User

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object SignInPage {

  case class Props(
                    router: RouterCtl[AppPage],
                    dispatcher: Dispatcher,
                    userProfile: Pot[User]
                  )

  case class State(
                    email: String,
                    password: String
                  )

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
      minHeight(450.px),
      width(400.px),
      alignItems.flexStart,
      float.none,
      margin(0.px, auto)
    )

    val links = style(
      marginTop(15.px),
      fontSize(20.px),
    )

    val dl = style(
      addClassName("row")
    )

    val dt = style(
      addClassName("col-sm-4"),
      textAlign.right,
      width(160.px)
    )

    val dd = style(
      addClassName("col-sm-8"),
      textAlign.left
    )
  }

  class Backend($: ScalaComponent.BackendScope[Props, State]) extends OnUnmount {

    // TODO real form validation
    def regularSignInForm(p: Props, s: State) = {
      Card()(
        if (p.userProfile.isFailed)
          CardHeader()("Sign In -- Failed!")
        else
          CardHeader()("Sign In")
        ,
        CardBody()(
          <.div(
            <.div(//bss.formGroup,
              <.label(^.`for` := "description", "Email"),
              <.input.text(
                //bss.formControl,
                ^.autoFocus := true,
                ^.id := "Email",
                ^.value := s.email,
                ^.placeholder := "",
                ^.onChange ==> { ev: ReactEventFromInput => val text = ev.target.value; $.modState(_.copy(email = text)) }
              )
            ),
            //        When(!Validation.isValidEmail(s.email) && s.email.nonEmpty)(
            //          <.div(s"Not a valid email: ${s.email}")
            //        ),
            // TODO add real validation
            <.div(//bss.formGroup,
              <.label(^.`for` := "description", "Password"),
              <.input.text(//bss.formControl,
                ^.id := "password",
                ^.value := s.password,
                ^.`type` := "password",
                ^.placeholder := "",
                ^.onChange ==> { ev: ReactEventFromInput => val text = ev.target.value; $.modState(_.copy(password = text)) }
              )
            ),
            Button(block = true, size = "lg", variant = "primary", disabled = !isValidSubmitState(s),
              onClick = () => Callback(p.dispatcher(SignIn(s.email, s.password)))
            )("Log In"),
            <.div(
              Style.links,
              p.router.link(RecoveryPg)("Forgot your password?"),
              <.br(),
              p.router.link(RegisterPg)("Create an account."),
            )
          )
        )
      )
    }

    def render(p: Props, s: State) = {
      <.div(
        p.userProfile.renderReady { up =>
          // This seems to be the easy way to redirect. but have to make sure to run AFTER rendering!
          // TODO fix this, is this where routes need props? so that the route redirects when you have a user profile?
          //p.router.set(MainPg).async.unsafeToFuture()
          <.div(
            Style.outerDiv,
            "Redirecting..."
          )
        },
        p.userProfile.renderPending { _ =>
          <.div(
            Style.outerDiv,
            <.h3("Signing In...")
          )
        },
        p.userProfile.renderFailed { ex =>
          <.div(Style.outerDiv,
            <.div(Style.innerDiv,
              regularSignInForm(p, s)
            )
          )
        },
        // Pending is conflated with Empty, so test state instead
        if (p.userProfile.state == PotEmpty) {
          <.div(Style.outerDiv,
            <.div(Style.innerDiv,
              regularSignInForm(p, s)
            )
          )
        } else EmptyVdom
      )
    }

    def isValidSubmitState(s: State): Boolean =
      Validation.isValidEmail(s.email) && s.password.nonEmpty

    def keydownEvent(e: dom.KeyboardEvent): Callback = {
      // Hook so that we try to sign in if the enter key is pressed
      for {
        state <- $.state
        props <- $.props
        _ <-
          if (e.key == "Enter" && isValidSubmitState(state)) {
            Callback(props.dispatcher(SignIn(state.email, state.password)))
          } else {
            Callback.empty
          }
      } yield ()
    }
  }

  // create the React component
  val component =
    ScalaComponent.builder[Props]("SignIn")
      .initialState(State("", ""))
      .renderBackend[Backend]
      .configure(EventListener[dom.KeyboardEvent].install("keydown", _.backend.keydownEvent))
      .build

  def apply(router: RouterCtl[AppPage],
            dispatcher: Dispatcher,
            userProfile: Pot[User]) =
    component(Props(router, dispatcher, userProfile))
}
