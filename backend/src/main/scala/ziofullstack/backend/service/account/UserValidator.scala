package ziofullstack.backend.service.account

import zio._
import ziofullstack.backend.infrastructure.db.repository.UserAccountRepo
import ziofullstack.backend.util.TaskE
import ziofullstack.shared.api._
import ziofullstack.shared.domain.User

trait UserValidator {
  def doesNotExist(user: User): TaskE[UserAlreadyExistsError, Unit]
  def exists(userId: Option[Long]): TaskE[UserNotFoundError.type, Unit]
}

object UserValidatorLive {
  val layer: URLayer[Has[UserAccountRepo], Has[UserValidator]] =
    (UserValidatorLive(_)).toLayer
}

case class UserValidatorLive(userRepo: UserAccountRepo) extends UserValidator {

  override def doesNotExist(user: User): TaskE[UserAlreadyExistsError, Unit] = {
    userRepo.findByEmail(user.email).map {
      case None => Right(())
      case Some(_) => Left(UserAlreadyExistsError(user))
    }
  }

  override def exists(userId: Option[Long]): TaskE[UserNotFoundError.type, Unit] = {
    userId.map { id =>
      userRepo.get(id).map {
        case Some(_) => Right(())
        case _ => Left(UserNotFoundError)
      }
    }.getOrElse(
      ZIO.left(UserNotFoundError)
    )
  }
}