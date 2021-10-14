package ziofullstack.backend.endpoints

import org.http4s.{EntityDecoder, EntityEncoder, HttpApp}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.headers.Authorization
import org.scalatest._
import funsuite._
import matchers._
import org.scalatest.matchers.should.Matchers
import shapeless.=:!=
import org.http4s.dsl.Http4sDsl
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import ziofullstack.backend.Arbitraries

import zio._

class EndpointsSpec extends AnyFunSuite
  with Matchers
  with Arbitraries
  with Http4sDsl[Task] {

//  def createAccountAndSignIn(
//                              authHeader: Authorization,
//                              newAccountInfo: AccountCreationRequest,
//                              endpoints: HttpApp[F]
//                            ): F[(OperatorProfile, Option[Authorization])] =
//    for {
//      signUpRq <- POST(newAccountInfo, uri"/operators", authHeader)
//      signUpResp <- endpoints.run(signUpRq)
//      profile <- signUpResp.as[OperatorProfile]
//      signInBody = admin.SignInRequest(profile.email, newAccountInfo.password)
//      signInRq <- POST(signInBody, uri"/auth/sign-in")
//      signInResp <- endpoints.run(signInRq)
//    } yield {
//      profile -> signInResp.headers.get(Authorization)
//    }

  test("create user") {
//    val userRepo = UserRepositoryInMemoryInterpreter[IO]()
//    val userValidation = UserValidationInterpreter[IO](userRepo)
//    val userService = UserService[IO](userRepo, userValidation, null)
//    val userHttpService = UserEndpoints.endpoints(userService).orNotFound
//
//    forAll { userSignup: RegistrationRequest =>
//      (for {
//        request <- POST(userSignup, uri"/users")
//        response <- userHttpService.run(request)
//      } yield {
//        response.status shouldEqual Ok
//      }).unsafeRunSync
//    }
  }

  test("update user") {
//    val userRepo = UserRepositoryInMemoryInterpreter[IO]()
//    val userValidation = UserValidationInterpreter[IO](userRepo)
//    val userService = UserService[IO](userRepo, userValidation, null)
//    val userHttpService = UserEndpoints.endpoints(userService).orNotFound
//
//    forAll { userSignup: RegistrationRequest =>
//      (for {
//        createRequest <- POST(userSignup, Uri.uri("/users"))
//        createResponse <- userHttpService.run(createRequest)
//        createdUser <- createResponse.as[User]
//        userToUpdate = createdUser.copy(lastName = createdUser.lastName.reverse)
//        updateUser <- PUT(userToUpdate, Uri.unsafeFromString(s"/users/${createdUser.userName}"))
//        updateResponse <- userHttpService.run(updateUser)
//        updatedUser <- updateResponse.as[User]
//      } yield {
//        updateResponse.status shouldEqual Ok
//        updatedUser.lastName shouldEqual createdUser.lastName.reverse
//        createdUser.id shouldEqual updatedUser.id
//      }).unsafeRunSync
//    }
  }

  test("get user by userName") {
//    val userRepo = UserRepositoryInMemoryInterpreter[IO]()
//    val userValidation = UserValidationInterpreter[IO](userRepo)
//    val userService = UserService[IO](userRepo, userValidation, null)
//    val userHttpService = UserEndpoints.endpoints(userService).orNotFound
//
//    forAll { userSignup: RegistrationRequest =>
//      (for {
//        createRequest <- POST(userSignup, Uri.uri("/users"))
//        createResponse <- userHttpService.run(createRequest)
//        createdUser <- createResponse.as[User]
//        getRequest <- GET(Uri.unsafeFromString(s"/users/${createdUser.userName}"))
//        getResponse <- userHttpService.run(getRequest)
//        getUser <- getResponse.as[User]
//      } yield {
//        getResponse.status shouldEqual Ok
//        createdUser.userName shouldEqual getUser.userName
//      }).unsafeRunSync
//    }
  }


  test("delete user by userName") {
//    val userRepo = UserRepositoryInMemoryInterpreter[IO]()
//    val userValidation = UserValidationInterpreter[IO](userRepo)
//    val userService = UserService[IO](userRepo, userValidation, null)
//    val userHttpService = UserEndpoints.endpoints(userService).orNotFound
//
//    forAll { userSignup: RegistrationRequest =>
//      (for {
//        createRequest <- POST(userSignup, Uri.uri("/users"))
//        createResponse <- userHttpService.run(createRequest)
//        createdUser <- createResponse.as[User]
//        deleteRequest <- DELETE(Uri.unsafeFromString(s"/users/${createdUser.userName}"))
//        deleteResponse <- userHttpService.run(deleteRequest)
//        getRequest <- GET(Uri.unsafeFromString(s"/users/${createdUser.userName}"))
//        getResponse <- userHttpService.run(getRequest)
//      } yield {
//        createResponse.status shouldEqual Ok
//        deleteResponse.status shouldEqual Ok
//        getResponse.status shouldEqual NotFound
//      }).unsafeRunSync
//    }
  }
}
