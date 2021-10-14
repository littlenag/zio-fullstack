package ziofullstack.backend.infrastructure.db.repository

import zio._
import ziofullstack.backend.infrastructure.db.domain.UserRow
import ziofullstack.backend.infrastructure.db.tables.Tables._
import ziofullstack.backend.infrastructure.db.tables.ZioInterop._
import ziofullstack.backend.service.exceptions.DbException
import ziofullstack.shared.domain.User


trait UserAccountRepo {
  def create(user: User): Task[User]
  def ensureUser(email:String, ifNotFound:User): Task[User]
  def update(user: User): Task[User]
  def get(id: Long): Task[Option[User]]
  def delete(id: Long): Task[Option[User]]
  def findByEmail(email: String): Task[Option[User]]
  def deleteByEmail(email: String): Task[Option[User]]
  def list(pageSize: Int, offset: Int): Task[Seq[User]]
}

object UserAccountRepoLive {
  val layer: URLayer[Has[SlickSession], Has[UserAccountRepo]] =
    {(db: SlickSession) => UserAccountRepoLive()(db)}.toLayer
}

case class UserAccountRepoLive()(implicit session: SlickSession) extends UserAccountRepo {

  import session.profile.api._

  def create(user: User): Task[User] = ZIO.fromDBIO_ { implicit ec =>
    val userWithId = (usersTable returning usersTable.map(_.id) into ((user, id) => user.copy(id = Some(id)))) += toRow(user)
    userWithId.map(fromRow)
  }

  /**
    * Ensure that a user account with email exists. If not, then create from the template provided.
    *
    * @param email
    * @param ifNotFound
    * @return
    */
  def ensureUser(email: String, ifNotFound: User): Task[User] = ZIO.fromDBIO_ { implicit ec =>
    usersTable.filter(_.email === email).result.headOption.flatMap {
      case Some(u) => DBIO.successful(fromRow(u))
      case None => ((usersTable returning usersTable.map(_.id) into ((user, id) => user.copy(id = Some(id)))) += toRow(ifNotFound)).map(fromRow)
    }.transactionally
  }

  def update(user: User): Task[User] = ZIO.fromDBIO_ { implicit ec =>
    user.id match {
      case None => DBIO.failed(DbException("Update requires id"))
      case Some(id) =>
        usersTable.filter(_.id === id).update(toRow(user)).flatMap {
          case 0 => DBIO.failed(DbException(s"Updated no user with id=$id"))
          case _ => DBIO.successful(())
        }.map(_ => user)
    }
  }

  def get(id: Long): Task[Option[User]] = ZIO.fromDBIO_ { implicit ec =>
    usersTable.filter(_.id === id).result.headOption.map(_.map(fromRow))
  }

  def findByEmail(email: String): Task[Option[User]] = ZIO.fromDBIO_ { implicit ec =>
    usersTable.filter(_.email === email).result.headOption.map(_.map(fromRow))
  }

  def delete(id: Long): Task[Option[User]] =
    get(id).flatMap { user =>
      ZIO.fromDBIO_ { implicit ec =>
        usersTable.filter(_.id === id).delete.map(_ => user)
      }
    }

  def deleteByEmail(email: String): Task[Option[User]] =
    findByEmail(email).flatMap { user =>
      ZIO.fromDBIO_ { implicit ec =>
        usersTable.filter(_.email === email).delete.map(_ => user)
      }
    }

  def list(pageSize: Int, offset: Int): Task[Seq[User]] = ZIO.fromDBIO_ { implicit ec =>
    usersTable.drop(offset).take(pageSize).result.map(rs => rs.map(fromRow))
  }

  private def toRow(u: User): UserRow = UserRow(u.id, u.email, u.firstName, u.lastName, u.hash, u.activated)

  private def fromRow(u: UserRow): User = User(u.email, u.firstName, u.lastName, u.hash, u.activated, u.id)
}