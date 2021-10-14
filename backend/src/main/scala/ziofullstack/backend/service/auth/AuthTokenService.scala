package ziofullstack.backend.service.auth

import zio._

import java.time.Instant
import java.time.temporal.ChronoUnit
import org.http4s.{HttpRoutes, Request, Response}
import tsec.common.{SecureRandomId, VerificationFailed, VerificationStatus, Verified}
import tsec.passwordhashers.{PasswordHash, PasswordHasher}
import tsec.passwordhashers.jca.{BCrypt, JCAPasswordPlatform}
import cats._
import cats.implicits._
import cats.data.{Kleisli, OptionT}
import tsec.authentication.{BackingStore, BearerTokenAuthenticator, SecuredRequest, SecuredRequestHandler, TSecAuthService, TSecBearerToken, TSecMiddleware, TSecTokenSettings}
import org.log4s.getLogger
import ziofullstack.backend.infrastructure.db.repository.{AuthTokenRepo, UserAccountRepo}
import ziofullstack.shared.api.PasswordResetRequest
import ziofullstack.shared.domain.User
import zio.interop.catz._
import zio.interop.catz.implicits._
import ziofullstack.backend.config.ConsoleConfig
import ziofullstack.backend.service.mailer.Mailer

import scala.concurrent.duration._
import scala.util.Random

object AuthTokenService {
  val layer: URLayer[Has[Mailer] with Has[UserAccountRepo] with Has[AuthTokenRepo] with Has[ConsoleConfig], Has[AuthTokenService]] =
    (AuthTokenService(_,_,_,_)).toLayer
}

case class AuthTokenService(
                             mailerService: Mailer,
                             userRepo: UserAccountRepo,
                             authInfoRepo: AuthTokenRepo,
                             consoleConfig: ConsoleConfig
                           ) {

  import courier._

  val appBaseUrl = consoleConfig.http.baseUrl

  type JCA = BCrypt
  val jcaPasswordPlatform: JCAPasswordPlatform[JCA] = BCrypt

  def logger = getLogger

  def mkResponseUrl(path: String): String = {
    if (path.startsWith("/"))
      appBaseUrl + path
    else
      appBaseUrl + "/" + path
  }

  def generateActivationToken: Task[String] =
    Task(Random.alphanumeric.take(20).mkString(""))

  def createActivationInfo(user: User): Task[AuthToken] = user.id match {
    case None =>
      Task.fail(new IllegalArgumentException(s"User must have ID."))
    case Some(userId) =>
      for {
        secureId <- generateActivationToken
        authInfo = AuthToken(
          secureId,
          userId,
          Instant.now().plus(3, ChronoUnit.DAYS),
          Option(Instant.now()),
          AuthTokenType.Activation
        )
        created <- authInfoRepo.create(authInfo)
      } yield created
  }

  def addressedEnvelope(user: User): Envelope =
    Envelope
      .from("ZIO Fullstack <noreply@markkegel.com>".addr)
      .to(user.email.addr)

  /**
    * Pre-conditions for the user:
    *   - exists
    *   - not yet active
    *   - has an activation token
    *
    * @param email
    * @return
    */
  def sendActivationEmail(email: String): Task[Unit] = {
    def emailBody(url: String) = {
      import scalatags.Text.all._

      val body = div(
        p(
          "To activate your QA Locate User Console account visit: ",
          a(href := url, url)
        )
      )

      Multipart().html(body.render)
    }

    logger.info(s"Send activation email $email")

    val action: Task[Unit] = for {
      maybeUser <- userRepo.findByEmail(email)
      _ = logger.debug(s"User $maybeUser for email $email")
      user <- maybeUser match {
        case Some(user) if !user.activated => ZIO.succeed(user)
        case _ =>
          Task.fail(
            new IllegalArgumentException("User is already active.")
          )
      }
      _ = logger.debug(s"Found user $user for email $email")
      maybeAuthInfo <- authInfoRepo.findByUserId(
        user.id.get,
        AuthTokenType.Activation.some
      )
      authInfo <- maybeAuthInfo match {
        case Some(authInfo) => ZIO.succeed(authInfo)
        case None =>
          createActivationInfo(user) // If the token was cleaned up, then just regen.
      }

      activationUrl = mkResponseUrl("/#/activation/" + authInfo.token)
      _ <- mailerService.send(
        addressedEnvelope(user)
          .subject("QA Locate - Account Activation")
          .content(emailBody(activationUrl))
      )

    } yield ()

    action orElse ZIO.unit
  }

  /**
    * Inform support staff that new account has been created. We should reach out to them!
    *
    * @param user
    * @return
    */
  def newUserNotification(user: User): Task[Unit] = {
    val emailBody = {
      import scalatags.Text.all._

      val body = div(
        s"New Account registered.",
        br,
        s"Timestamp: ${java.time.Instant.now().toString}",
        br,
        s"Email: ${user.email}",
        br,
        s"First Name: ${user.firstName}",
        br,
        s"Last Name: ${user.lastName}",
        br,
      )

      Multipart().html(body.render)
    }

    logger.info(s"Send new-user notification for user=$user")

    val action: Task[Unit] = for {
      _ <- mailerService.send(
        Envelope
          .from(user.email.addr)
          .to("New Account <support@markkegel.com>".addr)
          .subject(s"ZIO Fullstack - New Account Registered - ${user.email}")
          .content(emailBody)
      )
    } yield ()

    action orElse ZIO.unit
  }

  def processActivationToken(token: String): Task[Unit] = {
    for {
      maybeAuthInfo <- authInfoRepo.get(token, AuthTokenType.Activation.some)
      authInfo <- maybeAuthInfo match {
        case Some(authInfo) => ZIO.succeed(authInfo)
        case None =>
          Task.fail(
            new IllegalArgumentException("Invalid token.")
          )
      }

      maybeUser <- userRepo.get(authInfo.userId)
      user <- maybeUser match {
        case Some(user) if !user.activated => ZIO.succeed(user)
        case Some(user) if user.activated =>
          ZIO.fail(
            new IllegalArgumentException(
              "User already activated but had activation token !?!"
            )
          )
        case None =>
          ZIO.fail(
            new IllegalArgumentException(
              "No user for valid activation token !?!"
            )
          )
      }

      _ <- userRepo.update(user.copy(activated = true))

    } yield ()
  }

  ////

  def generateRecoveryToken: Task[String] =
    Task(java.util.UUID.randomUUID().toString)

  def createRecoveryInfo(user: User): Task[AuthToken] = user.id match {
    case None =>
      ZIO.fail(new IllegalArgumentException(s"User must have ID."))
    case Some(userId) =>
      for {
        secureId <- generateRecoveryToken
        authInfo = AuthToken(
          secureId,
          userId,
          Instant.now().plus(2, ChronoUnit.HOURS),
          Option(Instant.now()),
          AuthTokenType.Recovery
        )
        created <- authInfoRepo.create(authInfo)
      } yield created
  }

  /**
    * Pre-conditions for the user:
    *   - exists
    *   - must be active
    *   - has an activation token
    *
    * @param email
    * @return
    */
  def sendRecoveryEmail(email: String): Task[Unit] = {

    def emailBody(url: String): Content = {
      import scalatags.Text.all._

      val body = div(
        p(
          "To reset your QA Locate User Console password visit: ",
          a(href := url, url)
        )
      )

      Multipart().html(body.render)
    }

    val action: Task[Unit] = for {
      maybeUser <- userRepo.findByEmail(email)
      user <- maybeUser match {
        case Some(user) if user.activated => ZIO.succeed(user)
        case _                            => ZIO.fail(new IllegalArgumentException)
      }

      // generate a new recovery token every time
      authInfo <- createRecoveryInfo(user)

      recoveryUrl = mkResponseUrl("/#/password-reset/" + authInfo.token)
      _ <- mailerService.send(
        addressedEnvelope(user)
          .subject("QA Locate - Password Reset")
          .content(emailBody(recoveryUrl))
      )

    } yield ()

    action orElse ZIO.unit
  }

  def checkRecoveryToken(token: String): Task[Boolean] = {
    for {
      maybeAuthInfo <- authInfoRepo.get(token, AuthTokenType.Recovery.some)
    } yield maybeAuthInfo.isDefined
  }

  def processPasswordReset(passwordReset: PasswordResetRequest): Task[Unit] = {
    for {
      maybeAuthInfo <- authInfoRepo.get(passwordReset.resetToken, AuthTokenType.Recovery.some)
      authInfo <- maybeAuthInfo match {
        case Some(authInfo) => ZIO.succeed(authInfo)
        case None =>
          ZIO.fail(
            new IllegalArgumentException("Invalid token.")
          )
      }

      maybeUser <- userRepo.get(authInfo.userId)
      user <- maybeUser match {
        case Some(user) => ZIO.succeed(user)
        case None =>
          ZIO.fail(
            new IllegalArgumentException("No user for valid recovery token !?!")
          )
      }

      hashed <- hashPassword(passwordReset.newPassword)

      // Force activation since the user clearly was able to receive the recovery token. This fixes two issues
      //  1) User creates account but has a lapsed activation link and some how invokes a password reset rather than re-send activation email
      //  2) Smooths partial accounts we create for Map Search. Those accounts have no password, so the user has to reset the password regardless.
      _ <- userRepo.update(user.copy(activated = true, hash = hashed.toString))

    } yield ()
  }

  def processPasswordChange(user: User,
                            currentPassword: String,
                            newPassword: String): Task[User] = {
    for {
      status <- checkPassword(currentPassword, coerceToPasswordHash(user.hash))
      _ <- status match {
        case Verified => ZIO.unit
        case VerificationFailed =>
          ZIO.fail(
            new IllegalArgumentException("Current password invalid.")
          )
      }

      hashed <- hashPassword(newPassword)
      updatedUser <- userRepo.update(user.copy(hash = hashed.toString))
    } yield updatedUser
  }

  /**
    * Normal TSec helpers compose with an overly secure default. That is, they error out rather than trying subsequent routes. Thus we have to use our
    * own lifting mechanics to get somewhat more friendly behavior.
    *
    * The standard lifting functions can be accessed with the [[securedRequestHandler]].
    *
    * FIXME can be done a slightly different way
    *
    * @param pf   Route definition to lift.
    * @param ME   MonadError context to interpret in.
    * @return
    */
  def secureUserRoute(
    pf: PartialFunction[SecuredRequest[Task, User, TSecBearerToken[Long]], Task[
      Response[Task]
    ]]
  )(
    implicit ME: MonadError[Kleisli[OptionT[Task, ?], Request[Task], ?], Throwable]
  ): HttpRoutes[Task] = {
    liftWithPushthrough(TSecAuthService(pf))
  }

  private def liftWithPushthrough(
    service: TSecAuthService[User, TSecBearerToken[Long], Task]
  )(
    implicit ME: MonadError[Kleisli[OptionT[Task, ?], Request[Task], ?], Throwable]
  ): HttpRoutes[Task] = {

    val middleware: TSecMiddleware[Task, User, TSecBearerToken[Long]] =
      service => {
        Kleisli { r: Request[Task] =>
          Kleisli(securedRequestHandler.authenticator.extractAndValidate)
            .run(r)
            .flatMap(service.run)
        }
      }

    ME.handleErrorWith(middleware(service)) { e: Throwable =>
      logger.error(e)("Caught unhandled exception in authenticated service")
      Kleisli.liftF(OptionT.none)
    }
  }

  private val authInfoStore = {
    new BackingStore[Task, SecureRandomId, TSecBearerToken[Long]] {
      def get(id: SecureRandomId): OptionT[Task, TSecBearerToken[Long]] = {
        OptionT(
          authInfoRepo.get(id, AuthTokenType.Authentication.some).map(
            _.flatMap {
              case authInfo if authInfo.`type` == AuthTokenType.Authentication =>
                authInfo.asBearerToken.some
              case _ => none
            }
          )
        )
      }

      // Expects the element not to exist
      def put(elem: TSecBearerToken[Long]): Task[TSecBearerToken[Long]] = {
        authInfoRepo.create(AuthToken(elem)).flatMap {
          case authInfo if authInfo.`type` == AuthTokenType.Authentication =>
            ZIO.succeed(authInfo.asBearerToken)
          case _ =>
            ZIO.fail(new IllegalArgumentException)
        }
      }

      // Update if it already exists, otherwise create. More like upsert.
      def update(user: TSecBearerToken[Long]): Task[TSecBearerToken[Long]] = {
        authInfoRepo.update(AuthToken(user)).flatMap {
          case authInfo if authInfo.`type` == AuthTokenType.Authentication =>
            ZIO.succeed(authInfo.asBearerToken)
          case _ => ZIO.fail(new IllegalArgumentException)
        }
      }

      def delete(id: SecureRandomId): Task[Unit] = {
        authInfoRepo.delete(id, AuthTokenType.Authentication.some).flatMap {
          case Some(_) => ZIO.unit
          case None    => ZIO.fail(new IllegalArgumentException)
        }
      }
    }

  }

  private val userAccountStore = {
    new BackingStore[Task, Long, User] {
      def get(id: Long): OptionT[Task, User] = {
        OptionT(userRepo.get(id))
      }

      // Expects the element not to exist
      def put(elem: User): Task[User] = {
        userRepo.create(elem)
      }

      // Update if it already exists, otherwise create. More like upsert.
      def update(user: User): Task[User] = {
        userRepo.update(user).recoverWith {
          case _ => userRepo.create(user)
        }
      }

      def delete(id: Long): Task[Unit] = {
        userRepo.delete(id).flatMap {
          case Some(_) => ZIO.unit
          case None    => ZIO.fail(new IllegalArgumentException)
        }
      }
    }
  }

  private val settings =
    TSecTokenSettings(
      expiryDuration = 30.minutes, // Absolute expiration time
      maxIdle = None
    )

  private val bearerTokenAuth =
    BearerTokenAuthenticator(authInfoStore, userAccountStore, settings)

  val securedRequestHandler
    : SecuredRequestHandler[Task, Long, User, TSecBearerToken[Long]] =
    SecuredRequestHandler(bearerTokenAuth)

  private implicit def hasher: PasswordHasher[Task, JCA] =
    jcaPasswordPlatform.syncPasswordHasher[Task]

  def coerceToPasswordHash(raw: String): PasswordHash[JCA] =
    PasswordHash[JCA](raw)

  def checkPassword(password: String,
                    hash: PasswordHash[JCA]): Task[VerificationStatus] =
    jcaPasswordPlatform.checkpw[Task](password, hash)

  def hashPassword(password: String): Task[PasswordHash[JCA]] =
    jcaPasswordPlatform.hashpw(password)
}