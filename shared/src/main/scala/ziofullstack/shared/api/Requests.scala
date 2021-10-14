package ziofullstack.shared.api

import io.circe.generic._
import ziofullstack.shared.domain.User

import java.time.Instant
import scala.util.control.NoStackTrace

@JsonCodec
final case class SignInRequest(email: String, password: String)

// Authorizes the Frontend to talk with the Backend.
@JsonCodec
final case class FrontendAuthToken(value:String, expires: Instant)

@JsonCodec
final case class SignInResponse(user: User, auth: FrontendAuthToken)

@JsonCodec
final case class SignOutRequest(email: String)

@JsonCodec
final case class RegistrationRequest(email: String, firstName:String, lastName:String, password: String)

@JsonCodec
final case class ActivationEmailRequest(email: String)

@JsonCodec
final case class PasswordRecoveryRequest(email: String)

@JsonCodec
final case class PasswordResetRequest(newPassword: String, resetToken:String)

@JsonCodec
case class EmailChangeRequest(newEmail: String)

@JsonCodec
case class PasswordChangeRequest(currentPassword:String, newPassword: String)

//

sealed trait ValidationError extends Exception with Product with Serializable with NoStackTrace

case object UserTokenNotFoundError extends ValidationError
case object UserNotFoundError extends ValidationError
case object UserUpdateError extends ValidationError
case object UserInternalError extends ValidationError
case class UserAlreadyExistsError(user: User) extends ValidationError
case class UserAuthenticationFailedError(email: String) extends ValidationError