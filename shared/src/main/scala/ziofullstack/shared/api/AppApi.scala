package ziofullstack.shared.api

import ziofullstack.shared.domain.User

//private val `allow-origin` =
//Headers.serverSend("Access-Control-Allow-Origin", "*")
//
//// JWT via Bearer schema
//private val `Authorization` = Headers.client[String]("Authorization")
//
//private val baseHeaders = `allow-origin`


trait PublicApi[F[_]] {
  def signIn(req: SignInRequest): F[SignInResponse]
  def register(req: RegistrationRequest): F[User]
  def resendActivationEmail(req: ActivationEmailRequest): F[Unit]
  def activateAccount(activationToken: String): F[Unit]

  def sendPasswordRecoveryEmail(req: PasswordRecoveryRequest): F[Unit]
  def resetPassword(req: PasswordResetRequest): F[Unit]
  def validateRecoveryToken(recoveryToken: String): F[Unit]
}

// Api methods requiring authentication
trait SecureApi[F[_]] {
  def signOut(req: SignOutRequest): F[Unit]
  def refreshProfile(): F[User]
  def changeAccountEmail(req:EmailChangeRequest): F[Unit]
  def changeAccountPassword(req: PasswordChangeRequest): F[Unit]
  def deleteAccount(): F[Unit]
}
