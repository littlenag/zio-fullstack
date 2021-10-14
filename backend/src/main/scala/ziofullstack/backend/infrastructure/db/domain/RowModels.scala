package ziofullstack.backend.infrastructure.db.domain

import ziofullstack.backend.service.auth.AuthTokenType

import java.time.Instant

// No kind field identifies if recovery, activation, or other
case class AuthenticationTokenRow(
                            token: String,
                            userId: Long,
                            expiry: Instant,
                            lastTouched: Option[Instant],
                            kind: AuthTokenType
                          )

case class UserRow(
                    id: Option[Long],
                    email: String, // doubles as a username
                    firstName: String,
                    lastName: String,
                    hash: String,
                    activated: Boolean = false,
                  )


