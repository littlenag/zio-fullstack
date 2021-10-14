package ziofullstack.frontend

import io.github.littlenag.scalajs.components.`react-bootstrap`.{Card, CardBody, CardTitle}
import scalacss.ScalaCssReact._
import logstage._
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.html_<^._
import ziofullstack.frontend.css.GlobalStyles

package object components {
  /**
    * Info panels should be used for all main app content!
    * @param title
    * @param mods
    * @return
    */
  def InfoPanel(title: String)(mods: TagMod*): VdomNode = {
    InfoPanel((CardTitle()(title) +: mods): _*)
  }

  /**
    * Info panels should be used for all main app content!
    * @param mods
    * @return
    */
  def InfoPanel(mods: TagMod*): VdomNode = {
    Card()(GlobalStyles.infoPanel, CardBody()(mods: _*))
  }

  /**
    * Info panels should be used for all main app content!
    * @param mods
    * @return
    */
  def FullPagePanel(mods: TagMod*): VdomNode = {
    Card()(GlobalStyles.fullPagePanel, CardBody()(mods: _*))
  }
}
