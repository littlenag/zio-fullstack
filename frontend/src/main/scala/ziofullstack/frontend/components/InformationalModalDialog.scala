package ziofullstack.frontend.components

import io.github.littlenag.scalajs.components.`react-bootstrap`._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

/**
 * Open a Informational modal
 *
 * Example message:
 *    Thank you for renewing point-code.com!
 */
object InformationalModalDialog {

  case class Props(title:String, message:String, acknowledge: () => Callback)

  val component = ScalaFnComponent[Props](props =>
    Modal(
      show = true,
      onExit = props.acknowledge,
      onHide = props.acknowledge      // to handle clicking outside the modal
    )(
      ModalHeader()(
        ModalTitle()(props.title)
      ),
      ModalBody()(
        props.message
      ),
      ModalFooter()(
        Button(variant = "secondary", onClick = props.acknowledge)("Acknowledge")
      )
    )
  )

  def apply(title:String, message:String, acknowledge: () => Callback) =
    component(Props(title,message,acknowledge))
}
