package ziofullstack.backend

import java.time.Instant
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck._
import ziofullstack.shared.api.RegistrationRequest
import ziofullstack.shared.domain.User

trait Arbitraries {

  val emailGen = Gen.identifier.map(s => s + "@test.com")

  implicit val instant = Arbitrary[Instant] {
    for {
      millis <- Gen.posNum[Long]
    } yield Instant.ofEpochMilli(millis)
  }

  implicit val user = Arbitrary[User] {
    for {
      email <- emailGen
      firstName <- arbitrary[String]
      lastName <- arbitrary[String]
      password <- arbitrary[String]
      id <- Gen.posNum[Long]
    } yield User(email, firstName, lastName, password, true, Option(id))
  }

  implicit val userSignup = Arbitrary[RegistrationRequest] {
    for {
      userName <- emailGen
      password <- arbitrary[String]
    } yield RegistrationRequest(userName, "", "", password)
  }
}

object Arbitraries extends Arbitraries
