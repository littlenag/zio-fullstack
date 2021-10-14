package ziofullstack.backend.service.auth

import ziofullstack.backend.infrastructure.db.repository.AuthTokenRepo
import ziofullstack.backend.util.TaskE

trait AuthTokenValidator {
  def activationTokenExists(userId:Long): TaskE[Unit, AuthToken]
}

case class AuthTokenValidatorLive(authTokenRepo: AuthTokenRepo) {
  def activationTokenExists(userId:Long): TaskE[Unit, AuthToken] = {
    authTokenRepo.findByUserId(userId).map {
      case None => Left(())
      case Some(auth) => Right(auth)
    }
  }
}