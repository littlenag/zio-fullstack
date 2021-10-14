package ziofullstack.frontend.pages

import diode.data.Pot
import ziofullstack.frontend.AppRouter._
import diode.react._
import io.github.littlenag.scalajs.components.`react-bootstrap`.Button
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.internal.mutable.GlobalRegistry
import ziofullstack.frontend.css.CssSettings._
import scalacss.ScalaCssReact._
import ziofullstack.frontend.components.{Footer, InfoPanel}
import ziofullstack.frontend.model.Actions.{ActivationEmail, SetRouter}
import ziofullstack.frontend.util.ReactUtil.DispatcherCB
import ziofullstack.shared.domain.User

/**
  * Page we render if a user tries to access an auth protected page but hasn't yet logged in.
  */
object AppContainer {

  case class Props(router: RouterCtl[AppPage], resolution: Resolution[AppPage], user: ModelProxy[Pot[User]], dispatch: DispatcherCB)

  object Style extends StyleSheet.Inline {
    import dsl._

    // Padding to push context below the navbar
    val containerDiv = style(
      paddingTop(50.px),
      background := "linear-gradient(#01031a, #073254)",  // to bottom ??
      width(100.%%),
      maxWidth.inherit,
      height(100.%%),
      minHeight(100.%%)
    )

    val rowBody = style(
      minHeight(100.%%),
      margin(0.px),
    )

    val logo = style(
      padding(10.px),
      bottom(30.px),
      height(60.px),
      display.block,
      marginLeft.auto,
      marginRight.auto
    )
  }

  GlobalRegistry.register(Style)

  val Logo =
    <.div(
      <.img(Style.logo,
        ^.src := "/public/images/logo.png",
        ^.alt := "QA Locate"
      )
    )

  // create the React component for Home page
  private val component = ScalaComponent.builder[Props]("AppContainer")
    .initialState(false)
    .renderPS { ($, p, disableResendButton) =>

      val userProfilePot = p.user()

      val MainAppBody = <.div(Style.rowBody, ^.`class` := "row",
        <.div(^.`class` := "col-md-3 col-lg-2", AppSideNav(p.router)),
        <.main(^.`class` := "col-md-9 col-lg-10", p.resolution.render()),
      )

      val elems: Seq[TagMod] = p.resolution.page match {
        case _: SecurePage =>

          // If we try to render a page that requires authentication, but no user profile is set, then redirect to the sign-in page

          // If the user hasn't authenticated re-direct to the sign-in page
          if (userProfilePot.isEmpty) {
            // TODO figure this out for reals
            // Could maybe do this as an action? dispatch to a router in the app model?
            // This is now partially handled in the router itself via conditional routes
            p.router.set(SignInPg).async.unsafeToFuture()

            Seq(
              Logo,
              <.div(),
              Footer()
            )

          } else if (userProfilePot.map(! _.activated).getOrElse(false)) {

            // if not activated, then prompt to re-send activation email so they can activate
            Seq(
              Logo,
              InfoPanel("Your account is not yet activated.")(
                "You will need to activate your account before you can log in.",
                <.br,
                // link to re-send activation email
                Button(disabled = disableResendButton)(
                  ^.onClick --> {$.modState(_ => true) >> p.user.dispatchCB(ActivationEmail(userProfilePot.map(_.email).get))},
                  "Re-send activation email."
                )
              ),
              Footer()
            )
          } else {
            // Everything looks fine, render as normal
            Seq(
              AppTopNav(p.user, p.resolution.page, p.router),
              MainAppBody,
              Footer()
            )
          }

        // Unauthenticated pages get minimal additions, public pages may get rendered differently still
        case _: PublicPage =>
          Seq(
            Logo,
            <.div(p.resolution.render()),
            Footer()
          )
      }

      <.div.apply(
        Seq[TagMod](Style.containerDiv, ^.`class` := "container") ++ elems : _*
      )
    }
    .componentDidMount { $ =>
      $.props.dispatch.dispatchCB(SetRouter($.props.router))
    }
    .build

  def apply(router: RouterCtl[AppPage], r: Resolution[AppPage], user: ModelProxy[Pot[User]], dispatch: DispatcherCB) =
    component(Props(router, r, user, dispatch))
}
