package ziofullstack.backend.endpoints

import zio._
import org.http4s.HttpRoutes
import ziofullstack.backend.service.account.UserAccountsService
import ziofullstack.backend.service.auth.AuthTokenService
import ziofullstack.shared.api._
import ziofullstack.shared.domain.User
import org.typelevel.log4cats.slf4j.Slf4jLogger
import io.circe.syntax._
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import sloth.ServerFailure._
import tsec.authentication.{SecuredRequest, TSecBearerToken, asAuthed}
import tsec.common.Verified
import tsec.passwordhashers.PasswordHash
import ziofullstack.backend.util.ZLogger

import java.time.Instant

object RpcEndpoints {
  val layer: URLayer[Has[UserAccountsService] with Has[AuthTokenService] with Has[ZLogger], Has[RpcEndpoints]] =
    (RpcEndpoints(_,_,_)).toLayer
}


// https://zio.dev/docs/datatypes/contextual/#module-pattern-20
case class RpcEndpoints(userService: UserAccountsService,
                        authService: AuthTokenService,
                        logger: ZLogger
                       ) extends Http4sDsl[Task] {

  import sloth._
  import boopickle.Default._
  import chameleon.ext.boopickle._
  import io.circe._
  import io.circe.generic.auto._
  import chameleon.ext.circe._
  import java.nio.ByteBuffer
  import zio.interop.catz._
  import zio.interop.catz.implicits._

  implicit val instantPickler: boopickle.Pickler[Instant] =
    boopickle.DefaultBasic.longPickler.xmap(Instant.ofEpochMilli)(_.toEpochMilli)

  implicit private val jsoner = io.circe.Decoder.instance[Request[Json]] { cursor =>
    for {
      paths <- cursor.downField("path").as[List[String]]
      payload <- cursor.downField("payload").as[Json]
    } yield {
      Request(paths, payload)
    }
  }

  type F[A] = Task[A]

  type Auth = TSecBearerToken[Long]
  type SecuredUser = SecuredRequest[Task, User, TSecBearerToken[Long]]

  type FS[A] = RIO[Has[User] with Has[TSecBearerToken[Long]], A]

  implicit private val jj = jsonOf[Task, Request[Json]]

  lazy val routerPublicApi = Router[Json, F].route[PublicApi[F]](Api)
  lazy val routerSecureApi = Router[Json, FS].route[SecureApi[FS]](Api)

  implicit class RichSignUp(signupRequest: RegistrationRequest) {
    import signupRequest._

    // Create User from the SignupRequest
    def asUser[PA](hashedPassword: PasswordHash[PA]): User = User(
      email,
      firstName,
      lastName,
      hashedPassword,
      false // unactivated by default
    )
  }

  object Api extends PublicApi[Task] with SecureApi[FS] {
    def signIn(login: SignInRequest): F[SignInResponse] = {
      val email = login.email
      for {
        _ <- logger.debug(s"$login")
        user <- userService.getUserByEmail(email).absolve
        checkResult <- authService.checkPassword(
          login.password,
          authService.coerceToPasswordHash(user.hash)
        )

        user <- if (checkResult == Verified) {
          ZIO.succeed(user)
        } else
          ZIO.fail(UserAuthenticationFailedError(email))

        // Update the auth token
        token <- authService.securedRequestHandler.authenticator.create(user.id.get)

      } yield {
        SignInResponse(user, FrontendAuthToken(token.id, token.expiry))
      }
    }


    def register(signup: RegistrationRequest): F[User] = {
      for {
        hash <- authService.hashPassword(signup.password)
        userSpec = signup.asUser(hash)
        user <- userService.createUser(userSpec).absolve
        _ <- authService.createActivationInfo(user)
        _ <- authService.sendActivationEmail(user.email)
        _ <- authService.newUserNotification(user)
      } yield user
    }

    // re-send account activation email
    def resendActivationEmail(req: ActivationEmailRequest): F[Unit] = {
      authService.sendActivationEmail(req.email)
    }

    // process account activation token that was sent by email
    def activateAccount(activationToken: String): F[Unit] = {
      authService.processActivationToken(activationToken)
    }

    // re-send password recovery email
    def sendPasswordRecoveryEmail(req: PasswordRecoveryRequest): F[Unit] = {
      authService.sendRecoveryEmail(req.email)
    }

    def resetPassword(req: PasswordResetRequest): F[Unit] = {
      for {
        _ <- authService.processPasswordReset(req)
      } yield {}
    }

    // validate the password reset token, used by the client to either prompt for a new password, or inform that is invalid/expired
    def validateRecoveryToken(recoveryToken: String): F[Unit] = {
      authService.checkRecoveryToken(recoveryToken).flatMap { b =>
        if (b)
          ZIO.unit
        else
          ZIO.fail(new IllegalArgumentException(s"Invalid recovery token: $recoveryToken"))
      }
    }

    def signOut(req: SignOutRequest): FS[Unit] = {
      for {
        authenticator <- ZIO.access[Has[TSecBearerToken[Long]]](_.get)
        // remove the web token
        _ <- authService.securedRequestHandler.authenticator.discard(authenticator)
      } yield ()

    }

    def refreshProfile(): FS[User] = {
      for {
        user <- ZIO.access[Has[User]](_.get)
      } yield user
    }

    def changeAccountEmail(req: EmailChangeRequest): FS[Unit] = {
      for {
        user <- ZIO.access[Has[User]](_.get)
        result <- userService.update(user.copy(email = req.newEmail)).absolve
      } yield ()
    }

    def changeAccountPassword(req: PasswordChangeRequest): FS[Unit] = {
      for {
        user <- ZIO.access[Has[User]](_.get)
        _ <- authService.processPasswordChange(
          user,
          req.currentPassword,
          req.newPassword
        )
      } yield ()
    }

    def deleteAccount(): FS[Unit] = {
      for {
        user <- ZIO.access[Has[User]](_.get)
        _ <- userService.deleteByEmail(user.email)
      } yield ()
    }
  }

//  private val publicApiEndpoints: HttpRoutes[F] = {
//    HttpRoutes.of[F] {
//      case req @ POST -> Root / "rpc" / "public" =>
//        for {
//          logger <- Slf4jLogger.create[F]
//          bytes <- req.as[Array[Byte]]
//          s = Unpickle[Request[ByteBuffer]].fromBytes(ByteBuffer.wrap(bytes))
//          resp <-
//            routerPublicApi(s).toEither match {
//              case Right(value) =>
//                value.flatMap(bb => Ok(bb.array()))
//
//              // Treat as an error we can return to clients
//              case Left(HandlerError(ex)) =>
//                logger.info(ex)(s"Server failure - Handler Error") *>
//                  BadRequest(ex.getMessage)
//
//              case Left(PathNotFound(paths)) =>
//                logger.info(s"Server failure - Path Not Found - ${paths.mkString(" / ")}") *>
//                  Effect[F].raiseError(new RuntimeException(s"Path Not Found - ${paths.mkString(" / ")}"))
//
//              case Left(DeserializerError(ex)) =>
//                logger.info(ex)(s"Server failure - Deserializer Error") *>
//                  Effect[F].raiseError(ex)
//            }
//        } yield resp
//    }
//  }

  private val publicApiEndpoints: HttpRoutes[Task] = {
    HttpRoutes.of[Task] {
      case req @ POST -> Root / "rpc" / "public" =>
        for {
          logger <- Slf4jLogger.create[Task]
          decoded <- req.as[Request[Json]]
          resp <-
            routerPublicApi(decoded).toEither match {
              case Right(value) =>
                value.flatMap(bb => Ok(bb))

                // Treat as an error we can return to clients
              case Left(HandlerError(ex)) =>
                logger.info(ex)(s"Server failure - Handler Error") *>
                  BadRequest(ex.getMessage)

              case Left(PathNotFound(paths)) =>
                logger.info(s"Server failure - Path Not Found - ${paths.mkString(" / ")}") *>
                  ZIO.fail(new RuntimeException(s"Path Not Found - ${paths.mkString(" / ")}"))

              case Left(DeserializerError(ex)) =>
                logger.info(ex)(s"Server failure - Deserializer Error") *>
                  ZIO.fail(ex)
          }
        } yield resp
    }
  }

  private val secureApiEndpoints: HttpRoutes[Task] = {
    authService.secureUserRoute {
      case req @ POST -> Root / "rpc" / "secure" asAuthed _ =>
        for {
          logger <- Slf4jLogger.create[Task]
          decoded <- req.request.as[Request[Json]]
          resp <-
            routerSecureApi(decoded).toEither match {
              case Right(value) =>
                value
                  .provide(Has(req.authenticator) ++ Has(req.identity))
                  .flatMap(bb => Ok(bb))

              // Treat as an error we can return to clients
              case Left(HandlerError(ex)) =>
                logger.info(ex)(s"Server failure - Handler Error") *>
                  BadRequest(ex.getMessage)

              case Left(PathNotFound(paths)) =>
                logger.info(s"Server failure - Path Not Found - ${paths.mkString(" / ")}") *>
                  ZIO.fail(new RuntimeException(s"Path Not Found - ${paths.mkString(" / ")}"))

              case Left(DeserializerError(ex)) =>
                logger.info(ex)(s"Server failure - Deserializer Error") *>
                  ZIO.fail(ex)
            }
        } yield resp
    }
  }

  import cats.implicits._

  val endpoints: HttpRoutes[Task] =
    publicApiEndpoints <+> secureApiEndpoints
}