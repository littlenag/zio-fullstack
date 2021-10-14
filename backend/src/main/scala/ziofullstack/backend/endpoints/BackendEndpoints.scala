package ziofullstack.backend.endpoints

import zio._
import ziofullstack.misc.BuildInfo
import cats.implicits._
import cats.data.{NonEmptyList, OptionT}
import cats.effect.Async
import org.http4s.CacheDirective.`no-cache`
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, _}
import org.http4s.headers.{`Cache-Control`, `Content-Type`}
import org.http4s.server.staticcontent.WebjarServiceBuilder
import scalatags.Text.TypedTag
import ziofullstack.backend.config.ConsoleConfig
import ziofullstack.shared.domain.FrontendConfig
import zio.interop.catz._
import zio.interop.catz.implicits._
import ziofullstack.backend.util.ZLogger


object BackendEndpoints {
  val layer: URLayer[Has[ConsoleConfig] with Has[ZLogger], Has[BackendEndpoints]] =
    (BackendEndpoints(_,_)).toLayer
}

case class BackendEndpoints(consoleConfig: ConsoleConfig, logger: ZLogger) extends Http4sDsl[Task] {

  val publicAssetExtensions = List(
    ".html",
    ".js",
    ".css",
    ".map",
    ".ttf",
    ".woff",
    ".woff2",
    ".eot",
    ".svg",
    ".png",
    ".ico"
  )

  def getResource(pathInfo: String) =
    Task(getClass.getResource(pathInfo))

  /**
    * Base64 encoded version of client configuration included as part of the initially loaded HTML.
    */
  val base64UserConsoleConf = {
    import io.circe.syntax._

    val s = FrontendConfig(
      consoleConfig.env,
    )
    val str = s.asJson.noSpaces
    val enc = java.util.Base64.getEncoder

    new String(enc.encode(str.getBytes))
  }

  val indexHTML: TypedTag[String] = {
    import scalatags.Text.all._
    import scalatags.Text.tags2.title

    html(
      head(
        title("ZIO Fullstack | Frontend"),
        meta(charset := "UTF-8"),
      ),
      body(
        // div to stuff our config in
        div(
          id := "frontend-config",
          visibility.hidden,
          height := 0,
          width := 0,
          fontSize := 0
        )(base64UserConsoleConf),
        // div where our SPA is rendered.
        div(id := "app-container"),
        // Our app and deps
        script(
          `type` := "text/javascript",
          src := "/public/shared-bundle.js"
        ),
        script(
          `type` := "text/javascript",
          src := "/public/frontend-bundle.js"
        ),
      )
    )
  }

  val webjars =
    WebjarServiceBuilder[Task].withWebjarAssetFilter(
      asset =>
        // only allow js/font/image assets
        publicAssetExtensions.exists(ext => asset.asset.endsWith(ext))
    ).toRoutes(implicitly[Async[Task[*]]])

  val endpoints: HttpRoutes[Task] =
    HttpRoutes.of[Task] {
      // serves our index.html that starts our SPA
      case GET -> Root =>
        Ok(indexHTML.render)
          .map(
            _.withContentType(
              `Content-Type`(MediaType.text.html, Charset.`UTF-8`)
              ).putHeaders(`Cache-Control`(NonEmptyList.of(`no-cache`())))
          )

      case req @ GET -> "public" /: path =>
        for {
          _ <- logger.info(s"public resource: $path")
          r: Request[Task] = req.withPathInfo(
            Uri.Path.Root / Uri.Path.Segment(BuildInfo.name) / Uri.Path.Segment(BuildInfo.version) addSegments path.segments
          )
          rsp <- webjars(r).orElse(
            webjars(
              req.withPathInfo(
                  s"/${BuildInfo.name}/${BuildInfo.version}/public" + path.toString
              )
            )
          ).fold(NotFound())(x => ZIO.succeed(x))
            .flatten
        } yield rsp

      case req @ GET -> "resources" /: path
        if publicAssetExtensions.exists(req.pathInfo.toString().endsWith(_)) =>
        StaticFile
          .fromResource[Task](path.toString, req.some)
          .orElse(
            OptionT
              .liftF(getResource(path.toString))
              .flatMap(StaticFile.fromURL[Task](_, req.some))
          )
          .map(_.putHeaders(`Cache-Control`(NonEmptyList.of(`no-cache`()))))
          .fold(NotFound())(x => ZIO.succeed(x))
          .flatten
    }
}
