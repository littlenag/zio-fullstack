package ziofullstack.backend.infrastructure.db.repository

import zio._
import ziofullstack.backend.infrastructure.db.domain.AuthenticationTokenRow
import ziofullstack.backend.infrastructure.db.tables.Tables._
import ziofullstack.backend.infrastructure.db.tables.ZioInterop._
import ziofullstack.backend.service.auth.{AuthToken, AuthTokenType}

trait AuthTokenRepo {
  def create(user: AuthToken): Task[AuthToken]

  def update(user: AuthToken): Task[AuthToken]

  def get(token: String, `type`: Option[AuthTokenType] = None): Task[Option[AuthToken]]

  def delete(token: String, `type`: Option[AuthTokenType] = None): Task[Option[AuthToken]]

  def findByUserId(userId:Long, `type`: Option[AuthTokenType] = None): Task[Option[AuthToken]]
}

object AuthTokenRepoLive {
  val layer: URLayer[Has[SlickSession], Has[AuthTokenRepo]] =
    {(db: SlickSession) => AuthTokenRepoLive()(db)}.toLayer
}

case class AuthTokenRepoLive()(implicit slickSession: SlickSession) extends AuthTokenRepo {

  import slickSession.profile.api._

  def create(authInfo: AuthToken): Task[AuthToken] = ZIO.fromDBIO_ { implicit ec =>
    (authTokensTable += toRow(authInfo)).map(_ => authInfo)
  }

  def update(authInfo: AuthToken): Task[AuthToken] = ZIO.fromDBIO_ { implicit ec =>
    val action = authTokensTable.filter(_.token === authInfo.token).map(ai => (ai.expiry, ai.lastTouched))
    action.update((authInfo.expiry, authInfo.lastTouched)).map(_ => authInfo)
  }

  def get(token: String, `type`: Option[AuthTokenType] = None): Task[Option[AuthToken]] =
    ZIO.fromDBIO_ { implicit ec =>
      authTokensTable
        .filter(_.token === token)
        .filterIf(`type`.isDefined)(_.`type` === `type`.get)
        .result.headOption.map(_.map(fromRow))
    }

  def delete(id: String, `type`: Option[AuthTokenType] = None): Task[Option[AuthToken]] =
    get(id, `type`).flatMap {
      case None => ZIO.succeed(None)
      case Some(user) =>
        ZIO.fromDBIO_ { implicit ec =>
          val action = authTokensTable.filter(_.token === user.token).filter(_.`type` === user.`type`)
          action.delete.map(_ => Option(user))
        }
    }

  def findByUserId(userId: Long, `type`: Option[AuthTokenType] = None): Task[Option[AuthToken]] =
    ZIO.fromDBIO_ { implicit ec =>
      authTokensTable
        .filter(_.userId === userId)
        .filterIf(`type`.isDefined)(_.`type` === `type`.get)
        .result.headOption.map(_.map(fromRow))
    }

  private def toRow(ai: AuthToken): AuthenticationTokenRow = AuthenticationTokenRow(ai.token, ai.userId, ai.expiry, ai.lastTouched, ai.`type`)

  private def fromRow(a: AuthenticationTokenRow): AuthToken = AuthToken(a.token, a.userId, a.expiry, a.lastTouched, a.kind)
}