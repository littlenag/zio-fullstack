package ziofullstack.frontend.model

import diode.data.Pot
import japgolly.scalajs.react.extra.router.RouterCtl
import ziofullstack.frontend.AppRouter.AppPage
import ziofullstack.shared.domain.{FrontendConfig, User}

/**
  * Global application state.
  * TODO investigate react context https://reactjs.org/docs/context.html
  */
case class AppModel(config: FrontendConfig,
                    router: Option[RouterCtl[AppPage]],
                    user: Pot[User]
                   )
