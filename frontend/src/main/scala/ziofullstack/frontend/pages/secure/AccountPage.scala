package ziofullstack.frontend.pages.secure

import diode.Dispatcher
import io.github.littlenag.scalajs.components.`react-bootstrap`.{Button, Card, CardBody, CardTitle}
import scalacss.ScalaCssReact._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.internal.mutable.GlobalRegistry
import ziofullstack.frontend.css.CssSettings._
import ziofullstack.frontend.model.Actions.PasswordChange

/**
  * Base page for the application.
  */
object AccountPage {
  case class Props(dispatcher: Dispatcher)

  case class State(currentPassword:String, password1: String, password2: String)

  object Style extends StyleSheet.Inline {
    import dsl._

    val innerDiv = style(
      addClassName("container-fluid"),
      margin(10.px),
    )
  }

  GlobalRegistry.register(Style)

  def isValidSubmitState(s:State): Boolean = s.password1 == s.password2

  private val component = ScalaComponent.builder[Props]("AccountPage")
    .initialState(State("", "", ""))
    .renderPS { ($,p,s) =>
      <.div(Style.innerDiv,
        // change password
        // delete account?
        Card()(CardBody()(
          CardTitle()("API Access Tokens"),

          <.div(
            <.div(//bss.formGroup,
              <.label(^.`for` := "description", "Current Password"),
              <.input.text(//bss.formControl,
                ^.id := "current-password",
                ^.value := s.currentPassword,
                ^.`type` := "password",
                ^.placeholder := "",
                ^.onChange ==> {ev: ReactEventFromInput => val text = ev.target.value; $.modState(_.copy(currentPassword = text))}
              )
            ),
            <.div(//bss.formGroup,
              <.label(^.`for` := "description", "New Password"),
              <.input.text(//bss.formControl,
                ^.id := "password",
                ^.value := s.password1,
                ^.`type` := "password",
                ^.placeholder := "",
                ^.onChange ==> {ev: ReactEventFromInput => val text = ev.target.value; $.modState(_.copy(password1 = text))}
              )
            ),
            <.div(//bss.formGroup,
              <.label(^.`for` := "description", "Confirm Password"),
              <.input.text(//bss.formControl,
                ^.id := "password2",
                ^.value := s.password2,
                ^.`type` := "password",
                ^.placeholder := "",
                ^.onChange ==> {ev: ReactEventFromInput => val text = ev.target.value; $.modState(_.copy(password2 = text))}
              )
            ),
            Button(
              variant = "primary",
              disabled = !isValidSubmitState(s),
              onClick = () => Callback(p.dispatcher(PasswordChange(s.currentPassword,s.password1)))
            )("Save")
          )
        ))
      )


    }
    .build

  def apply(dispatcher: Dispatcher) =
    component(Props(dispatcher))
}
