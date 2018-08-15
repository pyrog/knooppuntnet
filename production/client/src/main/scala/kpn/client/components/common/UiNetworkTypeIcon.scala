package kpn.client.components.common

import chandu0101.scalajs.react.components.materialui.Mui.Styles.colors.grey800
import chandu0101.scalajs.react.components.materialui.Mui.SvgIcons.MapsDirectionsBike
import chandu0101.scalajs.react.components.materialui.Mui.SvgIcons.MapsDirectionsBoat
import chandu0101.scalajs.react.components.materialui.Mui.SvgIcons.MapsDirectionsWalk
import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.vdom.Implicits._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^.<
import japgolly.scalajs.react.vdom.html_<^.^
import kpn.client.components.home.UiIcon
import kpn.shared.NetworkType

import scala.scalajs.js

object UiNetworkTypeIcon {

  private case class Props(networkType: NetworkType)

  private val component = ScalaComponent.builder[Props]("network-type")
    .render_P { props =>
      val style1 = js.Dynamic.literal(
        verticalAlign = "text-bottom"
      )
      props.networkType match {
        case NetworkType.hiking => UiIcon(MapsDirectionsWalk, grey800, style1)
        case NetworkType.bicycle => UiIcon(MapsDirectionsBike, grey800, style1)
        case NetworkType.horse => // UiIcon(NotificationAirlineSeatFlatAngled, grey800, style1)
          <.img(
            ^.src := "/assets/images/horse.png",
            ^.width := "24",
            ^.height := "24"
          )
        case NetworkType.motorboat => UiIcon(MapsDirectionsBoat, grey800, style1)
        case NetworkType.canoe => // UiIcon(ActionRowing, grey800, style1)
          <.img(
            ^.src := "/assets/images/canoe.png",
            ^.width := "24",
            ^.height := "24"
          )
      }
    }
    .build

  def apply(networkType: NetworkType): VdomElement = component(Props(networkType))
}
