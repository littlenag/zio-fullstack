package ziofullstack.frontend.pages

import io.github.littlenag.scalajs.components.`react-bootstrap`.{Nav, NavItem, NavLink}
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._
import scalacss.internal.mutable.GlobalRegistry
import ziofullstack.frontend.AppRouter._
import ziofullstack.frontend.css.CssSettings._
import ziofullstack.frontend.util.FontAwesome

/**
  * Navigation menu for the application.
  *
  * Relies directly on bootstrap navbar being loaded.
  */
object AppSideNav {

  case class Props(ctrl: RouterCtl[AppPage])

  object Style extends StyleSheet.Inline {

    import dsl._

    val nav = style(
      marginTop(20.px),
      fontSize(24.px)
    )

    val link = style(
      color(c"rgb(244, 244, 244)")
    )
  }

  GlobalRegistry.register(Style)

  // create the React component
  val component = ScalaComponent
    .builder[Props]("SideNav")
    .render_P { p =>
      <.aside(
        Nav(variant = "pills")(
          Style.nav,
          ^.`class` := "ml-auto",
          ^.className := "flex-column",
          // TODO figure out actual highlight of nav links
          NavItem()(NavLink(href = p.ctrl.pathFor(MainPg).value)(Style.link, FontAwesome.fixedWidth("server"), " Overview")),
          NavItem()(NavLink(href = p.ctrl.pathFor(HelpPg).value)(Style.link, FontAwesome.fixedWidth("questionCircle")," Help")),
        )
      )
    }
    .build

  def apply(ctrl: RouterCtl[AppPage]) =
    component(Props(ctrl))

}
