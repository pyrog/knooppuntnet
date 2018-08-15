package kpn.client.components.route

import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.vdom.Implicits._
import japgolly.scalajs.react.vdom.TagMod
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^.<
import kpn.client.common.Context
import kpn.client.common.Nls
import kpn.client.common.Nls.nls
import kpn.client.components.common.UiImage
import kpn.client.components.common.UiMarked
import kpn.client.components.common.UiNetworkType
import kpn.client.components.common.UiOsmLink
import kpn.shared.Fact
import kpn.shared.route.RouteInfo

object UiRouteSummary {

  private case class Props(context: Context, routeInfo: RouteInfo)

  private val component = ScalaComponent.builder[Props]("route-summary")
    .render_P { props =>
      new Renderer(props.routeInfo)(props.context).render()
    }
    .build

  def apply(routeInfo: RouteInfo)(implicit context: Context): VdomElement = component(Props(context, routeInfo))

  private class Renderer(routeInfo: RouteInfo)(implicit context: Context) {

    private val summary = routeInfo.summary

    def render(): VdomElement = {
      if (routeInfo.ignored) {
        renderIgnored()
      }
      else {
        renderSummary()
      }
    }

    private def renderIgnored(): VdomElement = {
      <.div(
        relation(),
        networkType(),
        country(),
        nls(
          "This route is not included in the analysis.",
          "Deze route is niet opgenomen in de analyse."
        )
      )
    }

    private def renderSummary(): VdomElement = {
      <.div(
        meters(),
        relation(),
        networkType(),
        country(),
        broken(),
        incomplete()
      )
    }

    private def meters(): VdomElement = {
      <.p(summary.meters, " m")
    }

    private def relation(): VdomElement = {
      <.p(
        UiOsmLink.relation(summary.id),
        " (",
        UiOsmLink.josmRelation(summary.id),
        ")"
      )
    }

    private def networkType(): VdomElement = {
      UiNetworkType(summary.networkType)
    }

    private def country(): TagMod = {
      TagMod.when(summary.country.isDefined) {
        <.p(
          Nls.country(summary.country)
        )
      }
    }

    private def broken(): TagMod = {
      TagMod.when(routeInfo.facts.contains(Fact.RouteBroken)) {
        <.p(
          UiImage("warning.png"),
          " ",
          nls("This route seems broken.", "Er lijkt iets mis met deze route.")
        )
      }
    }

    private def incomplete(): TagMod = {
      TagMod.when(routeInfo.facts.contains(Fact.RouteIncomplete)) {
        <.p(
          UiImage("warning.png"),
          " ",
          UiMarked(
            nls(
              """Route definition is incomplete (has tag _"fixme=incomplete"_).""",
              """De route definitie is onvolledig (heeft tag _"fixme=incomplete"_)."""
            ),
            paragraphs = false
          )
        )
      }
    }
  }

}
