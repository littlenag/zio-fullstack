package ziofullstack.frontend.pages

import diode.react.ReactPot._
import diode.data.Pot
import diode.react.ModelProxy
import io.github.littlenag.scalajs.components.`react-bootstrap`._
import scalacss.ScalaCssReact._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.internal.mutable.{GlobalRegistry, StyleSheet}
import ziofullstack.frontend.AppRouter._
import ziofullstack.frontend.css.CssSettings._
import ziofullstack.frontend.util.FontAwesome
import ziofullstack.shared.domain.User

//import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

/**
  * Navigation menu for the application.
  *
  * Relies directly on bootstrap navbar being loaded.
  */
object AppTopNav {

  object Style extends StyleSheet.Inline {

    import dsl._

    val navBar = style(
      display.flex,
      paddingTop(0.px),
      paddingBottom(0.px),
      paddingLeft(20.px),
      paddingRight(20.px),
      margin.`0`,
      backgroundColor(c"#000000"),  // set background-color instead of using 'bg' prop
    )

    val navMenu = style(
      display.flex,
      alignItems.center,
      backgroundColor(c"#F2706D"),
      margin.`0`,
      listStyle := "none",
    )

    val link = style(
      color(c"rgb(244, 244, 244)")
    )

    val menuItem = style(
      fontSize(1.5.em),
      cursor.pointer,
      color(c"rgb(244, 233, 233)")
    )
  }

  GlobalRegistry.register(Style)

  case class Props(userProfile: ModelProxy[Pot[User]],
                   selectedPage: AppPage,
                   ctrl: RouterCtl[AppPage])

  class Backend($: ScalaComponent.BackendScope[Props, Unit]) {
    def unauthenticated(p: Props) = {
      Nav()(
        ^.`class` := "ml-auto",
        NavLink(href = p.ctrl.pathFor(SignInPg).value)(Style.link, "Sign In"),
        NavLink(href = p.ctrl.pathFor(RegisterPg).value)(Style.link, "Sign Up"),
      )
    }

    def authenticated(userProfile: User, p: Props) = {
      Nav()(
        ^.`class` := "ml-auto",
        NavDropdown(title = userProfile.email, alignRight = true)(
          ^.backgroundColor := "green",
          NavDropdownItem(href = p.ctrl.pathFor(AccountManagementPg).value)(FontAwesome.fixedWidth("user"), " Manage Account"),
          NavDropdownItem(href = p.ctrl.pathFor(SignOutPg).value)(FontAwesome.fixedWidth("powerOff"), " Sign Out")
        )
      )
    }

    def render(p: Props): VdomNode = {
      Navbar(variant = "dark", expand = "lg", fixed = "top")(
        Style.navBar,
        NavbarBrand(
          href = p.ctrl.pathFor(MainPg).value,
        )(
          <.img(
            ^.src := "/public/images/logo.png",
            ^.alt := "ZIO Fullstack"
          )
        ),
        p.userProfile().render { up => authenticated(up,p)},
        p.userProfile().renderEmpty { unauthenticated(p)}
      )
    }
  }

  // create the React component
  val component = ScalaComponent.builder[Props]("TopNav")
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[Pot[User]],
            selectedPage: AppPage,
            ctrl: RouterCtl[AppPage]) = component.apply(Props(proxy,selectedPage,ctrl))

}
