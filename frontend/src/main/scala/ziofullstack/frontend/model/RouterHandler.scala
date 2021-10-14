package ziofullstack.frontend.model

import diode.{ActionHandler, ModelRW}
import japgolly.scalajs.react.extra.router.RouterCtl
import logstage.IzLogger
import ziofullstack.frontend.AppRouter.AppPage
import ziofullstack.frontend.model.Actions.SetRouter

class RouterHandler[M](modelRW: ModelRW[M, Option[RouterCtl[AppPage]]])
  extends ActionHandler(modelRW) {

  val logger = IzLogger()

  override def handle = {
    case SetRouter(router) =>
      updated(Option(router))
  }
}
