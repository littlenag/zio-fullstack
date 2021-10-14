package ziofullstack.backend.infrastructure.db.tables

import java.time.Instant
import ziofullstack.backend.infrastructure.db.domain.{AuthenticationTokenRow, UserRow}
import ziofullstack.backend.service.auth.AuthTokenType
import enumeratum._

object Tables extends SlickEnumSupport {

  override val profile = slick.jdbc.PostgresProfile

  import profile.api._

  implicit val authInfoKindStatus: BaseColumnType[AuthTokenType] =
    mappedColumnTypeForEnum(AuthTokenType)

  //

  class UsersTable(tag: Tag) extends Table[UserRow](tag, "USERS") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def email = column[String]("EMAIL")
    def firstName = column[String]("FIRST_NAME")
    def lastName = column[String]("LAST_NAME")
    def hash = column[String]("PASSWORD_HASH")
    def activated = column[Boolean]("ACTIVATED")

    def * = (
      id.?, email, firstName, lastName, hash, activated
    ) <> (UserRow.tupled, UserRow.unapply)
  }

  class AuthenticationTokensTable(tag: Tag) extends Table[AuthenticationTokenRow](tag, "AUTH_TOKENS") {
    def token = column[String]("TOKEN", O.PrimaryKey)
    def userId = column[Long]("USER_ID")
    def `type` = column[AuthTokenType]("TYPE")
    def expiry = column[Instant]("EXPIRY")
    def lastTouched = column[Option[Instant]]("LAST_TOUCHED")

    def user=
      foreignKey("USER_ID_FK", userId, usersTable)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (token, userId, expiry, lastTouched, `type`) <> (AuthenticationTokenRow.tupled, AuthenticationTokenRow.unapply)
  }

  val usersTable = TableQuery[UsersTable]
  val authTokensTable = TableQuery[AuthenticationTokensTable]

}