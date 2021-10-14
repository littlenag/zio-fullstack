package ziofullstack.backend.service.auth

import java.time.Instant

import tsec.authentication.TSecBearerToken
import tsec.common.SecureRandomId
import enumeratum._

sealed trait AuthTokenType extends EnumEntry

case object AuthTokenType extends Enum[AuthTokenType] with CirceEnum[AuthTokenType] {
  case object Authentication extends AuthTokenType
  case object Activation extends AuthTokenType
  case object Recovery extends AuthTokenType

  val values = findValues
}

/**
  * Holds multiple kinds of authentication tokens. These tokens are used for
  * route authentication, account activation, and password recovery.
  *
  * @param id      Holds a SecureRandomId for route authentication, UUID-like string for account activation, UUID string for password recovery
  * @param userId
  * @param expiry
  * @param lastTouched
  */
case class AuthToken(
                      token: String,
                      userId: Long,
                      expiry: Instant,
                      lastTouched: Option[Instant],
                      `type`: AuthTokenType
                    ) {
  lazy val asBearerToken: TSecBearerToken[Long] =
    TSecBearerToken(SecureRandomId.coerce(token),userId,expiry,lastTouched)
}

object AuthToken {
  def apply(t: TSecBearerToken[Long]): AuthToken =
    AuthToken(t.id,t.identity,t.expiry,t.lastTouched,AuthTokenType.Authentication)
}