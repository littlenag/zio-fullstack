package ziofullstack.frontend.model

import diode._
import diode.data._
import japgolly.scalajs.react.extra.router.RouterCtl
import logstage._
import org.scalajs.dom.ext.AjaxException
import ziofullstack.frontend.AppRouter.{AppPage, MainPg}
import ziofullstack.frontend.model.Actions._
import ziofullstack.frontend.services.BackendClient
import ziofullstack.shared.api._
import ziofullstack.shared.domain.User

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

/**
  * Handles actions related to authentication
  *
  * @param modelRW Reader/Writer to access the model
  */
class AuthenticationHandler[M](modelRW: ModelRW[M, Pot[User]], router: ModelR[M, Option[RouterCtl[AppPage]]])
    extends ActionHandler(modelRW) {

  val logger = IzLogger()

  override def handle = {
    case PasswordChange(currentPassword,newPassword) =>
      effectOnly(
        Effect(
          BackendClient
            .secureApi
            .changeAccountPassword(PasswordChangeRequest(currentPassword,newPassword))
            .map {
              case _ =>
                AccountEditPersisted()
            }
            .recover {
              case ex: Exception =>
                // TODO create a bootstrap alert
                logger("ex" -> ex).warn("Unable to change password.")
                AccountEditError(ex)
            }
        )
      )

    case AccountEditPersisted() =>
      // TODO create a bootstrap alert
      noChange

    case AccountEditError(_) =>
      // TODO create a bootstrap alert
      noChange

      //

    case RefreshUserProfile =>
      effectOnly(
        Effect(
          BackendClient
            .secureApi
            .refreshProfile()
            .map {
              LoadedUserProfile
            }
            .recover {
              case ex: Exception =>
                logger("ex" -> ex).warn("Unable to refresh user profile.")
                NoUserProfile(ex)
            }
        )
      )

    case LoadedUserProfile(pd) =>
      updated(Ready(pd))

    case NoUserProfile(ex) =>
      updated(Failed(ex))

    case SignIn(email, password) =>
      logger.debug("Tried to sign in")
      updated(
        Pending(),
        Effect(
          BackendClient
            .publicApi
            .signIn(SignInRequest(email, password))
            .map { resp =>
              Authenticated(resp.user)
            }
            .recover { case x => SignInError(x) }
        )
      )
    case Authenticated(user) =>
      logger.debug("Sign in accepted")
      updated(Ready(user), Effect(
        router().get.set(MainPg).async.map(_ => NoAction).unsafeToFuture()
      ))

    case SignInError(ex: AjaxException) =>
      logger("exception" -> ex)
        .debug(s"Sign in failed: ${ex.xhr.responseText -> "responseText"}")
      updated(Failed(new RuntimeException(ex.xhr.responseText, ex)))

    case SignInError(ex) =>
      logger.debug("Sign in failed")
      updated(Failed(ex))

    case SignOut =>
      logger.debug("Signing out")
      updated(Empty)

    case Register(email, firstName, lastName, password) =>
      logger.debug("Attempting to register account")
      updated(
        Pending(),
        Effect(
          BackendClient
            .publicApi
            .register(
              RegistrationRequest(email, firstName, lastName, password)
            )
            .map { AccountCreated }
            .recover { case x => RegistrationError(x) }
        )
      )
    case AccountCreated(user) =>
      logger.debug("Registration accepted")
      updated(Ready(user))

    case RegistrationError(ex: AjaxException) =>
      logger("ex" -> ex).error(s"Registration failed: ${ex.xhr.responseText}")
      updated(Failed(new RuntimeException(ex.xhr.responseText, ex)))

    case RegistrationError(ex: Exception) =>
      logger("ex" -> ex).error("Registration failed")
      updated(Failed(ex))

    case RegistrationError(ex) =>
      logger.error("Registration failed: " + ex.getMessage)
      updated(Failed(ex))

    case PasswordReset(token, newPassword) =>
      logger.debug("Password reset")
      effectOnly(
        Effect(
          BackendClient
            .publicApi
            .resetPassword(PasswordResetRequest(newPassword, token))
            .map { _ =>
              NoAction
            }
        )
      )

    case PasswordRecovery(email) =>
      logger.debug("Password recovery email")
      effectOnly(
        Effect(
          BackendClient
            .publicApi
            .sendPasswordRecoveryEmail(PasswordRecoveryRequest(email))
            .map { _ =>
              NoAction
            }
        )
      )

    case ActivationEmail(email) =>
      logger.debug("Account activation email")
      effectOnly(
        Effect(
          BackendClient
            .publicApi
            .resendActivationEmail(ActivationEmailRequest(email))
            .map { _ =>
              NoAction
            }
        )
      )
  }
}