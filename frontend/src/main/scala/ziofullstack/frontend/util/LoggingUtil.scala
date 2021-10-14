package ziofullstack.frontend.util

import scala.scalajs.js

/**
  *
  */
object LoggingUtil {

  import logstage.IzLogger

  implicit class LogHelper(val izLogger: IzLogger) extends AnyVal {
    // Log an object directly to the console, generally allows object inspection
    def inspect(jsObject: js.Any): Unit = {
      org.scalajs.dom.console.log(jsObject)
    }
  }

}
