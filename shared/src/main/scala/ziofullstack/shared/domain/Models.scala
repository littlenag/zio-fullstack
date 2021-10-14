package ziofullstack.shared.domain

import io.circe.generic._
import enumeratum._
import enumeratum.EnumEntry._

sealed trait Env extends EnumEntry with Lowercase

object Env extends Enum[Env] with CirceEnum[Env] {
  case object Dev extends Env
  case object Test extends Env
  case object Prod extends Env

  val values = findValues
}

@JsonCodec
case class FrontendConfig(env: Env)

// TODO break into three parts based on https://twitter.com/buildsghost/status/1400630504957775872?s=19
// Higher kinded on id?
//  - “Identity” for authenticating
//  - “Actor” for performing actions
//  - “Role” for managing permissions

@JsonCodec
case class User(
                 email: String,
                 firstName: String,
                 lastName: String,
                 hash: String,              // password hash
                 activated: Boolean,

                 // Leave last
                 id: Option[Long] = None,
               )
