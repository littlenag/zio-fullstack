package ziofullstack.frontend

import zio._
import diode._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._
import ziofullstack.frontend.model.AppCircuit
import ziofullstack.frontend.pages.AppContainer
import ziofullstack.frontend.pages.public._
import ziofullstack.frontend.pages.secure._
import ziofullstack.frontend.util.ReactUtil.DispatcherCB
import ziofullstack.shared.domain.FrontendConfig

object AppRouter {
  // Define the page routes used in the App
  sealed trait AppPage
  sealed trait PublicPage extends AppPage
  sealed trait SecurePage extends AppPage

  // Public pages that anyone can access to sign in, sign out, etc
  case object SignInPg extends PublicPage
  case object SignOutPg extends PublicPage
  case object RegisterPg extends PublicPage
  case object RecoveryPg extends PublicPage
  case class PasswordResetPg(token:String) extends PublicPage
  case class AccountActivationPg(token:String) extends PublicPage

  case class EmailAddressActivationPg(token:String) extends PublicPage

  // Pages that only authenticated users should be able to access, others will be redirected.

  case object MainPg extends SecurePage
  case object HelpPg extends SecurePage
  case object AccountManagementPg extends SecurePage
}

case class AppRouter(consoleConfig: FrontendConfig) {
  import AppRouter._

  // configure the router
  val routerConfig = RouterConfigDsl[AppPage].buildConfig { dsl =>
    import dsl._

    implicit val defaultMethod = SetRouteVia.HistoryReplace

    val circuit = new AppCircuit(consoleConfig)

    val dispatcher: DispatcherCB = new DispatcherCB {
      override def dispatch[A: ActionType](action: A): Unit =
        circuit.dispatch(action)
    }

    val userProfileWrapper = circuit.connect(_.user)
    def userProfile = circuit.zoom(_.user).value

    def layout(c: RouterCtl[AppPage], r: Resolution[AppPage]) = {
      // Render the app inside a react element so that we can catch top-level things like trying to access
      // pages that need authentication when you haven't yet logged in.
      userProfileWrapper(AppContainer(c,r,_,dispatcher))
    }

    // Account sign-in/sign-out pages
    val accountPages = ( emptyRule
      | staticRoute("#/sign-in", SignInPg) ~> renderR(ctl => SignInPage(ctl,dispatcher,userProfile))
      | staticRoute("#/sign-out", SignOutPg) ~> renderR(ctl => SignOutPage(ctl,dispatcher))
      | staticRoute("#/register", RegisterPg) ~> renderR(ctl => userProfileWrapper(RegistrationPage(ctl,_)))
      | staticRoute("#/recovery", RecoveryPg) ~> renderR(ctl => RecoveryPage(ctl,dispatcher))
      | dynamicRouteCT("#/password-reset" / remainingPath.caseClass[PasswordResetPg]) ~> dynRenderR((rt: PasswordResetPg, ctl) => PasswordResetPage(ctl,rt.token,dispatcher))
      | dynamicRouteCT("#/activation" / remainingPath.caseClass[AccountActivationPg]) ~> dynRenderR((rt: AccountActivationPg, ctl) => AccountActivationPage(ctl,rt.token))
      )

    val appPages = ( emptyRule
      | staticRoute("#/overview", MainPg) ~> render(MainPage())
      | staticRoute("#/help", HelpPg) ~> render(HelpPage())
      | staticRoute("#/account", AccountManagementPg) ~> render(AccountPage(dispatcher))
      )
      .addConditionWithFallback(CallbackTo(userProfile.isReady), redirectToPage(SignInPg))

    val allPages = ( emptyRule
      | accountPages
      | appPages
      )

    allPages
      .notFound(redirectToPage(SignInPg))
      .logToConsole
      .renderWith(layout)
  }

  // create the router
  val router: Router[AppPage] = Router(BaseUrl.until_#, routerConfig)
}
