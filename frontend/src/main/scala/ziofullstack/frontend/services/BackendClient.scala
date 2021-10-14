package ziofullstack.frontend.services

import boopickle.Default._
import cats.MonadError

import java.time.Instant
import scala.scalajs.js.typedarray.TypedArrayBuffer
import scala.scalajs.js.typedarray.ArrayBuffer
import io.circe.{Json, JsonObject}
import io.circe.syntax._
import io.circe.parser.parse
import io.lemonlabs.uri.Url
import japgolly.scalajs.react.AsyncCallback
import japgolly.scalajs.react.extra.Ajax
import japgolly.scalajs.react.CatsReact._
import org.scalajs.dom
import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.ext.Ajax.InputData
import org.scalajs.dom.ext.{Ajax => JSAjax}
import ziofullstack.shared.api._

import java.nio.ByteBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * TODO maybe return AsyncCallback instead of Future?
 */
object BackendClient {

  // https://github.com/scala-js/scala-js-dom/issues/201
  private val getOrigin = {
    if (dom.window.location.origin.isDefined) {
      dom.window.location.origin.get
    } else {
      val port = if (dom.window.location.port.nonEmpty) ":" + dom.window.location.port else ""
      dom.window.location.protocol + "//" + dom.window.location.hostname + port
    }
  }

  private var bearerToken: Option[FrontendAuthToken] = None

  // If you don't want to use local storage because it's too insecure, then store in memory.
  def setBearerToken(authToken: FrontendAuthToken) = bearerToken = Some(authToken)
  def getBearerToken: Option[FrontendAuthToken] = bearerToken

  // If you are comfortable with local storage, then we use these.
  //val CLIENT_AUTH_TOKEN = "app-auth-token"
  //def storeBearerToken(authToken: FrontendAuthToken) = dom.window.localStorage.setItem(CLIENT_AUTH_TOKEN, authToken.value)
  //def retrieveBearerToken: Option[FrontendAuthToken] = Option(dom.window.localStorage.getItem(CLIENT_AUTH_TOKEN)).map(FrontendAuthToken(_))

  // Shove our authorization token in the bearer header
  private def clientAuthHeader = {
    getBearerToken.map(token => "Bearer " + token.value).getOrElse("")
  }

  // Does the work of running the typed api requests
  //private val cm = ClientManager(Ajax, getOrigin)

  private def postBytes[A](url:Url, data: ByteBuffer, public: Boolean = true)(decoder: ArrayBuffer => Either[Throwable, A]): Future[A] = {
    val p = Ajax.post(url.toString())
    val p2 =
      if (public)
        p
      else
        p.setRequestHeader("Authorization", clientAuthHeader)

    p2
      .setRequestHeader("Content-Type", "application/octet-stream")
      .send(JSAjax.InputData.byteBuffer2ajax(data))
      .asAsyncCallback
      .flatMap { xml =>
        decoder(xml.response.asInstanceOf[ArrayBuffer]) match {
          case Right(value) => AsyncCallback.pure[A](value)
          case Left(er) => AsyncCallback.throwException[A](er)
        }
      }
      .unsafeToFuture
  }

  private def postJson(url:Url, data: Json, public: Boolean = true): Future[XMLHttpRequest] = {
    val p = Ajax.post(url.toString())
    val p2 =
      if (public)
        p
      else
        p.setRequestHeader("Authorization", clientAuthHeader)

    p2
      .setRequestContentTypeJsonUtf8
      .send(JSAjax.InputData.str2ajax(data.noSpaces))
      .asAsyncCallback
      .unsafeToFuture
  }

  import sloth._
  //import boopickle.Default._
  import chameleon.ext.boopickle._
  import chameleon.ext.circe._
  import java.nio.ByteBuffer
  import cats.implicits._

  //private type PickleType = Json
  //private type EffectType[A] = Future[A]

  implicit val instantPickler: boopickle.Pickler[Instant] =
    boopickle.DefaultBasic.longPickler.xmap(Instant.ofEpochMilli)(_.toEpochMilli)

  implicit private val pickler = generatePickler[Request[ByteBuffer]]

  private def transportBytes(ep:String, public:Boolean) =
    new RequestTransport[ByteBuffer, Future] {
      // implement the transport layer. this example just calls the router directly.
      // in reality, the request would be sent over a connection.
      override def apply(request: Request[ByteBuffer]): Future[ByteBuffer] = {
        val x = Pickle.intoBytes(request)

        // getting base64 text?
        postBytes(Url(path = ep), x, public) { response =>
          Right(TypedArrayBuffer.wrap(response))
        }
      }
    }

  implicit private val jsoner = io.circe.Encoder.instance[Request[Json]] { r =>
    Json.obj(
      "path" -> Json.arr(r.path.map(Json.fromString):_*),
      "payload" -> r.payload
    )
  }

  private def transportJson(ep:String, public:Boolean) =
    new RequestTransport[Json, Future] {
      // implement the transport layer. this example just calls the router directly.
      // in reality, the request would be sent over a connection.
      override def apply(request: Request[Json]): Future[Json] = {
        val x = request.asJson

        postJson(Url(path = ep), x, public).flatMap { xml =>
          parse(xml.responseText) match {
            case Left(value) => Future.failed(value)
            case Right(value) => Future.successful(value)
          }
        }
      }
    }

  import ziofullstack.shared.api._
  import io.circe.generic.auto._

  private val client1 = Client[Json, Future, ClientException](transportJson("/rpc/public", true))
  val publicApi: PublicApi[Future] = client1.wire[PublicApi[Future]]

  private val client2 = Client[Json, Future, ClientException](transportJson("/rpc/secure", false))
  val secureApi: SecureApi[Future] = client2.wire[SecureApi[Future]]


  // ENDPOINTS: AUTHENTICATION

}