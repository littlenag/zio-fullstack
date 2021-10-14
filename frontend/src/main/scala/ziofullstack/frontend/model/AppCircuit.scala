package ziofullstack.frontend.model

import diode._
import diode.data._
import diode.react.ReactConnector
import ziofullstack.shared.domain.FrontendConfig

// Application circuit
class AppCircuit(consoleConfig: FrontendConfig)
    extends Circuit[AppModel]
    with ReactConnector[AppModel] {

  // initial application model, fields available once the user logs in
  override protected def initialModel =
    AppModel(consoleConfig, None, Empty)

  // combine all handlers into one
  override protected val actionHandler = composeHandlers(
    new AuthenticationHandler(
      zoomTo(_.user),
      zoom(_.router)
    ),
    new RouterHandler(
      zoomTo(_.router)
    )
  )
}
