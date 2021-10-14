package ziofullstack.frontend.model

import diode.Action
import japgolly.scalajs.react.extra.router.RouterCtl
import ziofullstack.frontend.AppRouter.AppPage
import ziofullstack.shared.domain._

/**
  * Every Diode action that our application may generate.
  */
object Actions {
  // Actions :: App Setup
  case class SetRouter(router: RouterCtl[AppPage]) extends Action

  // Actions :: User Profile management
  case object RefreshUserProfile extends Action
  case class LoadedUserProfile(userProfile: User) extends Action
  case class NoUserProfile(ex: Throwable) extends Action

  // Actions :: Authentication
  case class SignIn(email: String, password: String) extends Action
  case class Authenticated(user: User) extends Action
  case class SignInError(ex: Throwable) extends Action

  case object SignOut extends Action

  case class Register(
                       email: String,
                       firstName: String,
                       lastName: String,
                       password: String
                     )
      extends Action
  case class AccountCreated(user: User) extends Action
  case class RegistrationError(ex: Throwable) extends Action

  case class PasswordRecovery(email: String) extends Action
  case class PasswordReset(token: String, newPassword: String) extends Action

  case class ActivationEmail(email: String) extends Action

  // Account Actions

  case class PasswordChange(currentPassword: String, newPassword: String) extends Action
  case class AccountEditPersisted() extends Action
  case class AccountEditError(ex: Throwable) extends Action
}
