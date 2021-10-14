package ziofullstack.backend.service.account

import cats.implicits._
import zio._
import ziofullstack.backend.config.ConsoleConfig
import ziofullstack.backend.infrastructure.db.repository.UserAccountRepo
import ziofullstack.backend.service.account.UserValidator
import ziofullstack.backend.service.mailer.Mailer
import ziofullstack.backend.util.{TaskE, ZLogger}
import ziofullstack.shared.api._
import ziofullstack.shared.domain.User

trait UserAccountsService {
  def createUser(user: User): TaskE[UserAlreadyExistsError, User]
  def ensureUserAccount(email: String): TaskE[UserInternalError.type, User]
  def getUser(userId: Long): TaskE[UserNotFoundError.type, User]
  def getUserByEmail(email: String): TaskE[UserNotFoundError.type, User]
  def deleteUser(userId: Long): Task[Unit]
  def deleteByEmail(email: String): Task[Unit]
  def update(user: User): TaskE[UserUpdateError.type, User]
  def list(pageSize: Int, offset: Int): Task[Seq[User]]
}

object UserAccountsServiceLive {
  val layer: URLayer[Has[UserAccountRepo] with Has[UserValidator] with Has[Mailer] with Has[ConsoleConfig] with Has[ZLogger], Has[UserAccountsService]] =
    (UserAccountsServiceLive(_,_,_,_,_)).toLayer
}

case class UserAccountsServiceLive(userRepo: UserAccountRepo,
                                   validation: UserValidator,
                                   mailerService: Mailer,
                                   config: ConsoleConfig,
                                   logger: ZLogger) extends UserAccountsService {


  def createUser(user: User): TaskE[UserAlreadyExistsError, User] =
    for {
      _ <- validation.doesNotExist(user)
      saved <- userRepo.create(user).map(Right(_))
    } yield saved

  /**
    * Accounts created in this way are generally from folks creating PointCodes. Creating a PointCode requires an email.
    * And that email is used to instantiate a "shadow" account on the platform, which is what we are doing here.
    *
    * This "shadow" account, however, should NOT be activated by default. To be promoted to a real account will require
    * an email link along with a password reset.
    *
    * @param email
    * @return
    */
  def ensureUserAccount(
                         email: String
                       ): TaskE[UserInternalError.type, User] = {
    // Having a blank password will cause an exception if the user tries to actually sign in, which is what we want.
    userRepo.ensureUser(email, User(email, "", "", "", false))
      .either
      .map(_.leftMap(_ => UserInternalError))
  }

  def getUser(userId: Long): TaskE[UserNotFoundError.type, User] =
    userRepo.get(userId).map(_.toRight(UserNotFoundError))

  def getUserByEmail(
                      email: String
                    ): TaskE[UserNotFoundError.type, User] =
    userRepo.findByEmail(email).map(_.toRight(UserNotFoundError))

  def deleteUser(userId: Long): Task[Unit] = userRepo.delete(userId).map(_ => ())

  def deleteByEmail(email: String): Task[Unit] =
    userRepo.deleteByEmail(email).as(())

  def update(user: User): TaskE[UserUpdateError.type, User] = {
    for {
      _ <- validation.exists(user.id).absolve
      saved <- userRepo.update(user).either

      user <- saved match {
        case Left(ex) =>
          logger.error(s"Unable to update user: $user", Cause.fail(ex)) *>
            ZIO.succeed(Left(UserUpdateError))
        case Right(user) =>
          ZIO.succeed(Right(user))
      }
    } yield user
  }

  def list(pageSize: Int, offset: Int): Task[Seq[User]] =
    userRepo.list(pageSize, offset)
}