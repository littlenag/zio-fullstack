package ziofullstack.frontend.pages.public

import diode.react.ReactPot._
import diode.data.Pot
import diode.data.PotState.PotEmpty
import diode.react._
import io.github.littlenag.scalajs.components.`react-bootstrap`.{Button, Card, CardBody, CardHeader}
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.{EventListener, OnUnmount}
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^.{^, _}
import org.scalajs.dom
import scalacss.internal.mutable.GlobalRegistry
import scalacss.ScalaCssReact._
import ziofullstack.frontend.AppRouter.{AppPage, MainPg, SignInPg}
import ziofullstack.frontend.css.CssSettings._
import ziofullstack.frontend.model.Actions.Register
import ziofullstack.frontend.util.ReactUtil._
import ziofullstack.frontend.util.Validation
import ziofullstack.shared.domain.User

import scala.language.existentials

object RegistrationPage {

  // referral contains affiliate info as base64 encoded JSON document
  case class Props(router: RouterCtl[AppPage], userProfile: ModelProxy[Pot[User]])

  case class State(email:String, firstName:String, lastName:String, password1: String, password2: String)

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
      margin(17.px)
    )
  }

  class Backend($: ScalaComponent.BackendScope[Props, State]) extends OnUnmount {

    // TODO do real form validation
    def signUpForm(p: Props, s: State) = {
      <.div(
        <.div(//bss.formGroup,
          <.label(^.`for` := "description", "Email"),
          <.input.text(//bss.formControl,
            ^.id := "email",
            ^.value := s.email,
            ^.placeholder := "",
            ^.onChange ==> {ev: ReactEventFromInput => val text = ev.target.value; $.modState(_.copy(email = text))}
          )
        ),
        When(!Validation.isValidEmail(s.email) && s.email.nonEmpty)(
          <.div(s"Not a valid email: ${s.email}")
        ),
        <.div(//bss.formGroup,
          <.label(^.`for` := "description", "First Name"),
          <.input.text(//bss.formControl,
            ^.id := "first-name",
            ^.value := s.firstName,
            ^.placeholder := "",
            ^.onChange ==> {ev: ReactEventFromInput => val text = ev.target.value; $.modState(_.copy(firstName = text))}
          )
        ),
        <.div(//bss.formGroup,
          <.label(^.`for` := "description", "Last Name"),
          <.input.text(//bss.formControl,
            ^.id := "last-name",
            ^.value := s.lastName,
            ^.placeholder := "",
            ^.onChange ==> {ev: ReactEventFromInput => val text = ev.target.value; $.modState(_.copy(lastName = text))}
          )
        ),
        <.div(//bss.formGroup,
          <.label(^.`for` := "description", "Password"),
          <.input.password(//bss.formControl,
            ^.id := "password",
            ^.value := s.password1,
            ^.placeholder := "",
            ^.onChange ==> {ev: ReactEventFromInput => val text = ev.target.value; $.modState(_.copy(password1 = text))}
          )
        ),
        <.div(//bss.formGroup,
          <.label(^.`for` := "description", "Confirm Password"),
          <.input.password(//bss.formControl,
            ^.id := "password2",
            ^.value := s.password2,
            ^.placeholder := "",
            ^.onChange ==> {ev: ReactEventFromInput => val text = ev.target.value; $.modState(_.copy(password2 = text))}
          )
        ),
        Button(block = true,
          size = "lg",
          variant = "primary",
          disabled = !isValidSubmitState(s),
          onClick = () => p.userProfile.dispatchCB(Register(s.email, s.firstName, s.lastName, s.password1)))("Submit"),
        <.div(
          Style.links,
          <.span("Already a member? ", p.router.link(SignInPg)("Sign in now"))
        )
      )
    }

    def render(p: Props, s: State) = {
      <.div(
        p.userProfile().renderReady { up =>
          if (up.activated) {
            // If we're already activated, then immediately re-direct.
            // This seems to be the easy way to redirect. but have to make sure to run AFTER rendering!
            // TODO fix
            p.router.set(MainPg).async.unsafeToFuture()
            <.div(Style.outerDiv,
              <.div(Style.innerDiv,
                Card()(
                  CardHeader()("Sign Up"),
                  CardBody()("Redirecting to home page.")
                )
              )
            )
          } else {
            // Tell user they'll have to activate their account
            <.div(Style.outerDiv,
              <.div(Style.innerDiv,
                Card()(
                  CardHeader()("Sign Up"),
                  CardBody()(
                    "Your account has been created. An activation email should appear in the next few minutes."
                  )
                )
              )
            )
          }
        },
        p.userProfile().renderPending { _ =>
          <.div(Style.outerDiv,
            <.div(Style.innerDiv,
              Card()(
                CardHeader()("Sign Up"),
                CardBody()("Creating your account...")
              )
            )
          )
        },
        p.userProfile().renderFailed { ex =>
          <.div(Style.outerDiv,
            <.div(Style.innerDiv,
              Card()(
                CardHeader()(s"Sign Up -- There was an error: ${ex.getMessage}"),
                CardBody()(signUpForm(p,s))
              )
            )
          )
        },
        // Pending is conflated with Empty, so test state instead
        if (p.userProfile().state == PotEmpty) {
          <.div(Style.outerDiv,
            <.div(Style.innerDiv,
              Card()(
                CardHeader()(s"Sign Up"),
                CardBody()(signUpForm(p,s))
              )
            )
          )
        }
        else EmptyVdom
      )
    }

    def isValidSubmitState(s:State): Boolean =
      s.email.nonEmpty && Validation.isValidEmail(s.email) &&  // must have valid email
        s.password1.nonEmpty && s.password1 == s.password2     // must have password

    def keydownEvent(e: dom.KeyboardEvent): Callback = {
      // Hook so that we try to sign in if the enter key is pressed
      for {
        state <- $.state
        props <- $.props
        _ <-
          if (e.key == "Enter" && isValidSubmitState(state)) {
            props.userProfile.dispatchCB(Register(state.email, state.firstName, state.lastName, state.password1))
          } else {
            Callback.empty
          }
      } yield ()
    }
  }

    // create the React component for Dashboard
  private val component = ScalaComponent.builder[Props]("Registration")
    // create and store the connect proxy in state for later use
    .initialState(State("", "", "", "", ""))
    .renderBackend[Backend]
    .configure(EventListener[dom.KeyboardEvent].install("keydown", _.backend.keydownEvent))
    .build

  def apply(router: RouterCtl[AppPage], proxy: ModelProxy[Pot[User]]) =
    component(Props(router, proxy))
}
