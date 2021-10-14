package ziofullstack.frontend.components

import io.github.littlenag.scalajs.components.`react-bootstrap`._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

/**
 * Open a Yes/No modal
 *
 * Example message:
 *    Renew point-code.com right now? This can NOT be undone. This does NOT include any web hosting charges.
 */
object ProceedModalDialog {

  case class Props(title:String, message:String, cancel: () => Callback, proceed: () => Callback)

  val component = ScalaFnComponent[Props](props =>
    Modal(
      show = true,
      onExit = props.cancel,
      onHide = props.cancel      // to handle clicking outside the modal
    )(
      ModalHeader()(
        ModalTitle()(props.title)
      ),
      ModalBody()(
        props.message
      ),
      ModalFooter()(
        Button(variant = "secondary", onClick = props.cancel)("Close"),
        Button(variant = "primary", onClick = () => props.proceed())("Proceed")
      )
    )
  )

  def apply(title:String, message:String, cancel: () => Callback, proceed: () => Callback) =
    component(Props(title,message,cancel,proceed))
}
