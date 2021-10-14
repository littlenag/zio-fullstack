package ziofullstack.frontend

import scalacss.internal.mutable.GlobalRegistry

/**
  *
  */
package object css {
  val CssSettings = scalacss.devOrProdDefaults

  def onStartup() = {
    import CssSettings._

    GlobalRegistry.register(GlobalStyles)
    GlobalRegistry.onRegistration(_.addToDocument())
  }
}

