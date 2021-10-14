package ziofullstack.backend.config

import ziofullstack.shared.domain.Env

import zio.config._
import zio.config.ConfigDescriptor._
//import zio.config.typesafe._
import zio.config.magnolia._

final case class ConsoleConfig(env: Env, http: HttpServerConfig, infrastructure: InfrastructureConfigs)

object ConsoleConfig {

 implicit val envConfig: ConfigDescriptor[Env] =
  string("env").transform(Env.withNameInsensitive, _.entryName)

 implicit val zioConfigDescriptor: ConfigDescriptor[ConsoleConfig] = descriptor[ConsoleConfig]
}

// Http - http server configs
final case class HttpServerConfig(baseUrl: String, host: String, port: Int, logRequests:Boolean, logResponses:Boolean)

// Services - services we provide
// TODO add petstore services in

// Infrastructure - things we depend on
final case class InfrastructureConfigs(smtp: SmtpConfig, aws: AwsConfig)
final case class SmtpConfig(host: String, port: Int, user: String, password: String, startTls: Option[Boolean], mock: Boolean)
final case class AwsConfig(`creds-provider`:String, accessKey:Option[String], secretKey:Option[String], region:Option[String])
