package ziofullstack.backend

import zio._
import zio.magic._
import com.typesafe.config.ConfigFactory
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware._
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.{Router, Server}
import ziofullstack.backend.config.ConsoleConfig
import ziofullstack.backend.endpoints.{BackendEndpoints, RpcEndpoints}
import ziofullstack.backend.infrastructure.db.migration.MigrationAssistant
import ziofullstack.backend.infrastructure.db.repository._
import ziofullstack.backend.service.account._
import ziofullstack.backend.service.auth.AuthTokenService
import ziofullstack.backend.service.mailer.MailerLive
import ziofullstack.backend.util._
import ziofullstack.shared.domain._
import zio.interop.catz._
import zio.interop.catz.implicits._
import zio.logging.slf4j._
import zio.logging._

import scala.concurrent.ExecutionContext
import zio.config.typesafe.TypesafeConfig

/**
 *
 */
object Main extends zio.App {

  def run(args : List[String]) : URIO[ZEnv, ExitCode] = {
    wiredApp.exitCode
  }

  val logFormat = "[correlation-id = %s] %s"

  val logging =
    Slf4jLogger.make { (context, message) =>
      val correlationId = context(LogAnnotation.CorrelationId)
      logFormat.format(correlationId, message)
    }

  // Determine the environment the application is running in
  val env = (
    for {
      env <- inferEnv
      _ <- Logging.info(s"Environment: $env")
    } yield env
    ).toLayer

  val typesafeConfig = (for {
    env <- ZIO.service[Env]
    _ <- Logging.info(s"Loading config: application.${env.entryName}.conf")
    rawConfig = ConfigFactory.load(s"application.${env.entryName}.conf")

  } yield rawConfig.getConfig("ziofullstack")).toLayer

  //
  // Load Config
  //
  val appConfig =
    TypesafeConfig.fromTypesafeConfigM(ZIO.service[com.typesafe.config.Config], ConsoleConfig.zioConfigDescriptor)

  val smtpConfig =
    ZLayer.service[ConsoleConfig].project(_.infrastructure.smtp)

  //
  // Setup and migrate database
  //
  val dbSession = (for {
    rawConfig <- ZIO.service[com.typesafe.config.Config]
    db <- Task(SlickSession.forConfig("infrastructure.db", rawConfig))

    _ <- Logging.info("Starting database migration...")

    assistant <- MigrationAssistant.live().provide(Has(db))
    _ <- assistant.clean()
    _ <- assistant.migrate()
    _ <- Logging.info("Finished database migration...")

  } yield db).toLayer


  val httpServer = (for {
    consoleConfig <- ZIO.service[ConsoleConfig].toManaged_

    rpc <- ZIO.service[RpcEndpoints].toManaged_

    backend <- ZIO.service[BackendEndpoints].toManaged_

    userEndpoints = CORS(Router(
      "/" -> rpc.endpoints,
      "/" -> backend.endpoints
    )).orNotFound |> {
      RequestLogger.httpApp(consoleConfig.http.logRequests,consoleConfig.http.logRequests)
    }

    httpServer <-
      BlazeServerBuilder[Task](ExecutionContext.global)
        .bindHttp(consoleConfig.http.port, consoleConfig.http.host)
        .withHttpApp(userEndpoints)
        .resource
        .toManagedZIO

  } yield {
    httpServer
  }).toLayer

  object TestUser

  val testUser = (for {
    authTokenService <- ZIO.service[AuthTokenService]
    userAccountsService <- ZIO.service[UserAccountsService]
    env <- ZIO.service[Env]

    // Create a test operator and user accounts while in DEV mode
    _ <-
      if (env == Env.Dev) {
        for {
          userPass <- authTokenService.hashPassword("test")
          _ <- userAccountsService.createUser(User("test@test.com", "", "", userPass, true, None)).absolve
        } yield ()
      } else {
        ZIO.unit
      }

  } yield TestUser).toLayer

  val program =
    ZIO.services[Server, TestUser.type] *> ZIO.never

  val wiredApp =
    program.inject(
      testUser,
      ZEnv.live,
      httpServer,
      RpcEndpoints.layer,
      BackendEndpoints.layer,
      UserAccountRepoLive.layer,
      AuthTokenRepoLive.layer,
      UserValidatorLive.layer,
      MailerLive.layer,
      AuthTokenService.layer,
      UserAccountsServiceLive.layer,
      dbSession,
      appConfig,
      smtpConfig,
      typesafeConfig,
      env,
      logging
    )

}