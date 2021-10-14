package ziofullstack.frontend

import ziofullstack.frontend.util.ScalaUtil._
import logstage.IzLogger
import org.scalajs.dom
import ziofullstack.frontend.util.FontAwesome
import ziofullstack.frontend.util.LoggingUtil.LogHelper
import ziofullstack.shared.domain.Env.Dev
import ziofullstack.shared.domain.{Env, FrontendConfig}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportTopLevel, JSImport}

/**
  * Entry point for the Frontend
  */
@JSExportTopLevel("Main")
object Main {

  val logger = IzLogger()

  @js.native
  @JSImport("jquery", JSImport.Namespace)
  object jqueryRequire extends js.Object

  @js.native
  @JSImport("bootstrap", JSImport.Namespace)
  object bootstrapRequire extends js.Object

  @js.native
  @JSImport("bootstrap/dist/css/bootstrap.css", JSImport.Namespace)
  object bootstrapCssRequire extends js.Object

  @js.native
  @JSImport("react", JSImport.Namespace)
  object reactRequire extends js.Object

  def useNpmImports(): Unit = {
    // Load FontAwesome (font icons)
    FontAwesome.CssImport.discard

    jqueryRequire.discard
    bootstrapRequire.discard
    bootstrapCssRequire.discard
    reactRequire.discard
  }

  def main(args: Array[String]): Unit = {

    // Load/link our NPM/JS deps
    useNpmImports()

    ///
    /// At this point all our app deps have been loaded and we good to start initializing
    /// our application itself.
    ///

    logger.debug("QA Locate Customer Portal App Started.")

    val appConfig = {
      import io.circe._, parser._
      val configElement =
        dom.document.getElementById("frontend-config").textContent
      val json = new String(java.util.Base64.getDecoder.decode(configElement))
      decode[FrontendConfig](json).getOrElse {
        logger.debug(
          "Unable to load configuration. Proceeding with default config."
        )
        FrontendConfig(Dev)
      }
    }

    if (appConfig.env == Env.Dev)
      logger.inspect(appConfig.asInstanceOf[js.Any])

    logger.debug(s"App Mode: ${appConfig.env}")

    // create stylesheet
    css.onStartup()

    // tell React to render the router in the document body
    val appRouter = AppRouter(appConfig)

    appRouter
      .router()
      .renderIntoDOM(dom.document.getElementById("app-container"))
  }
}
