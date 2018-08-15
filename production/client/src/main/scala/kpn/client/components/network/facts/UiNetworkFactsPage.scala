package kpn.client.components.network.facts

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.Implicits._
import japgolly.scalajs.react.vdom.TagMod
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^.<
import japgolly.scalajs.react.vdom.html_<^.^
import kpn.client.api.ApiClient
import kpn.client.common.NetworkPageArgs
import kpn.client.common.Nls.nls
import kpn.client.common.Nls.nlsNL
import kpn.client.components.common.AbstractBackend
import kpn.client.components.common.PageState
import kpn.client.components.common.PageStatus
import kpn.client.components.common.UiCommaList
import kpn.client.components.common.UiDetail
import kpn.client.components.common.UiFactLevel
import kpn.client.components.common.UiItems
import kpn.client.components.common.UiOsmLink
import kpn.client.components.common.UiPageContents
import kpn.client.components.common.UiSituationOn
import kpn.client.components.common.UiThick
import kpn.client.components.common.UiThin
import kpn.client.components.facts.UiFactDescription
import kpn.client.components.home.Loader
import kpn.client.components.network.NetworkNameCache
import kpn.client.components.network.NetworkPageRenderer
import kpn.client.components.network.NetworkSummaryCache
import kpn.client.components.network.UiNetworkMenu
import kpn.shared.ApiResponse
import kpn.shared.Fact
import kpn.shared.NetworkExtraMemberNode
import kpn.shared.NetworkExtraMemberRelation
import kpn.shared.NetworkExtraMemberWay
import kpn.shared.NetworkFacts
import kpn.shared.NetworkIntegrityCheckFailed
import kpn.shared.NetworkNameMissing
import kpn.shared.network.NetworkFactsPage
import kpn.shared.network.NetworkRouteFact

object UiNetworkFactsPage {

  private case class State(pageState: PageState[NetworkFactsPage] = PageState())

  private class Backend(scope: BackendScope[NetworkPageArgs, State]) extends AbstractBackend[NetworkFactsPage] {

    protected def pageState: PageState[NetworkFactsPage] = scope.state.runNow().pageState

    protected def updatePageState(pageState: PageState[NetworkFactsPage]): Unit = {
      scope.modState(_.copy(pageState = pageState)).runNow()
    }

    def render(args: NetworkPageArgs, state: State): VdomElement = {

      val pageProps = pagePropsWithContext(args.context)

      val networkInfo = if (state.pageState.response.isDefined) {
        val ni = state.pageState.response.get.result.get.networkSummary
        NetworkSummaryCache.put(args.networkId, ni)
        NetworkNameCache.put(args.networkId, ni.name)
        Some(ni)
      }
      else {
        NetworkSummaryCache.get(args.networkId)
      }

      val props = NetworkFactsProps(
        args,
        pageProps,
        NetworkNameCache.get(args.networkId),
        networkInfo
      )

      new Renderer(props, state).render()
    }

    def retrieve(props: NetworkPageArgs): Unit = {

      scope.modState(s => s.copy(pageState = s.pageState.copy(ui = s.pageState.ui.copy(status = PageStatus.LoadStarting, isSideBarOpen = false), response = None)))
        .runNow()

      def updatePageStatus(status: PageStatus.Value): Unit = {
        scope.modState(s => s.copy(pageState = s.pageState.withStatus(status))).runNow()
      }

      def updateResult(response: ApiResponse[NetworkFactsPage]): Unit = {
        scope.modState(s => s.copy(pageState = s.pageState.withResponse(response))).runNow()
      }

      new Loader[ApiResponse[NetworkFactsPage]].load(
        ApiClient.networkFacts(props.networkId),
        PageStatus.LoadStarting,
        updatePageStatus,
        updateResult
      )
    }
  }

  private val component = ScalaComponent.builder[NetworkPageArgs]("network-facts")
    .initialState(State())
    .renderBackend[Backend]
    .componentWillReceiveProps { scope =>
      Callback {
        scope.backend.retrieve(scope.nextProps)
      }
    }
    .componentDidMount { scope =>
      Callback {
        scope.backend.installResizeListener()
        scope.backend.retrieve(scope.props)
      }
    }
    .componentWillUnmount { scope =>
      scope.backend.removeResizeListener()
    }
    .build

  def apply(props: NetworkPageArgs): VdomElement = component(props)

  private class Renderer(props: NetworkFactsProps, state: State)
    extends NetworkPageRenderer(UiNetworkMenu.facts, state.pageState.ui.status, props.networkName, props.networkSummary)(props) {

    protected def contents(): VdomElement = {
      <.div(
        <.h1(state.pageState.response.get.result.get.networkSummary.name),
        <.h2(nls("Facts", "Feiten")),
        UiPageContents(
          UiSituationOn(state.pageState.situationOn),
          allFacts()
        )
      )
    }

    private def situationOn(): TagMod = {
      TagMod.when(state.pageState.response.get.situationOn.isDefined) {
        <.p(
          nls("Situation on", "Situatie op"),
          ": ",
          state.pageState.response.get.situationOn.get.yyyymmddhhmm
        )
      }
    }

    private def allFacts(): VdomElement = {
      val page = state.pageState.response.get.result.get
      if (page.factCount == 0) {
        <.div(
          <.br(),
          nls("no facts", "geen feiten")
        )
      }
      else {
        UiItems(
          Seq(
            factDetails(page.facts),
            networkFactDetails(page.networkFacts),
            routeFactDetails(page.routeFacts)
          ).flatten
        )
      }
    }

    private def networkFactDetails(networkFacts: NetworkFacts): Seq[TagMod] = {
      Seq(
        nameMissing(networkFacts.nameMissing),
        networkExtraMemberNode(networkFacts.networkExtraMemberNode),
        networkExtraMemberWay(networkFacts.networkExtraMemberWay),
        networkExtraMemberRelation(networkFacts.networkExtraMemberRelation),
        integrityCheckFailed(networkFacts.integrityCheckFailed)
      ).flatten
    }

    private def routeFactDetails(routeFacts: Seq[NetworkRouteFact]): Seq[TagMod] = {
      routeFacts.map(factRow)
    }

    private def factRow(networkFact: NetworkRouteFact): TagMod = {
      <.div(
        UiThick(
          <.span(
            nls(networkFact.fact.name, networkFact.fact.nlName) + " (" + networkFact.routes.size + ") "
          )
        ),
        UiFactLevel(networkFact.fact.level),
        UiFactDescription(networkFact.fact),
        <.div(
          UiThin(
            if (networkFact.routes.size > 1) {
              "Routes: "
            } else {
              "Route: "
            }
          ),
          UiCommaList(
            for (route <- networkFact.routes) yield {
              context.gotoRoute(route.id, route.name)
            }
          )
        )
      )
    }

    private def factDetails(facts: Seq[Fact]): Seq[TagMod] = {
      facts.map { fact =>
        <.div(
          UiThick(
            <.span(
              nls(fact.name, fact.nlName), " "
            )
          ),
          UiFactLevel(fact.level),
          UiFactDescription(fact)
        )
      }
    }

    private def networkExtraMemberNode(networkExtraMemberNode: Option[Seq[NetworkExtraMemberNode]]): Seq[TagMod] = {
      networkExtraMemberNode.toSeq.map { networkExtraMemberNodes =>
        <.div(
          UiThick(
            <.span(
              nls("NetworkExtraMemberNode", "NetwerkExtraNode"),
              " (",
              networkExtraMemberNodes.size,
              ")"
            )
          ),
          networkExtraMemberNodes.toTagMod { networkExtraMemberNode =>
            UiDetail(
              <.div(
                UiOsmLink.osmNode(networkExtraMemberNode.memberId),
                " (",
                UiOsmLink.josmNode(networkExtraMemberNode.memberId),
                ")"
              )
            )
          }
        )
      }
    }

    private def networkExtraMemberWay(networkExtraMemberWay: Option[Seq[NetworkExtraMemberWay]]): Seq[TagMod] = {
      networkExtraMemberWay.toSeq.map { networkExtraMemberWays =>
        <.div(
          UiThick(
            <.span(
              nls("NetworkExtraMemberWay", "NetwerkExtraWay"),
              " (",
              networkExtraMemberWays.size,
              ")"
            )
          ),
          networkExtraMemberWays.toTagMod { networkExtraMemberWay =>
            UiDetail(
              <.div(
                UiOsmLink.osmWay(networkExtraMemberWay.memberId),
                " (",
                UiOsmLink.josmWay(networkExtraMemberWay.memberId),
                ")"
              )
            )
          }
        )
      }
    }

    private def networkExtraMemberRelation(facts: Option[Seq[NetworkExtraMemberRelation]]): Seq[TagMod] = {
      facts.toSeq.map { networkExtraMemberRelations =>
        <.div(
          UiThick(
            <.span(
              nls("NetworkExtraMemberRelation", "NetwerkExtraRelatie"),
              " (",
              networkExtraMemberRelations.size,
              ")"
            )
          ),
          networkExtraMemberRelations.toTagMod { networkExtraMemberRelation =>
            UiDetail(
              <.div(
                UiOsmLink.osmRelation(networkExtraMemberRelation.memberId),
                " (",
                UiOsmLink.josmRelation(networkExtraMemberRelation.memberId),
                ")"
              )
            )
          }
        )
      }
    }

    private def integrityCheckFailed(fact: Option[NetworkIntegrityCheckFailed]): Seq[TagMod] = {
      fact.toSeq.map { networkIntegrityCheckFailed =>
        val failedChecks = networkIntegrityCheckFailed.checks.filter(_.failed)
        TagMod.when(failedChecks.nonEmpty) {
          <.div(
            UiThick(
              <.span(
                nls("IntegrityCheckFailed", "OnverwachtRouteAantal") + " (" + failedChecks.size + ")"
              )
            ),
            <.p(
              if (nlsNL) {
                "Het aantal routes die in het knooppunt aankomen of vertrekken is niet het verwachte aantal."
              } else {
                "The actual number of routes does not match the expected number of routes."
              }
            ),
            <.table(
              ^.title := "node integrity check failures",
              <.thead(
                <.tr(
                  <.th(nls("Node", "Knooppunt")),
                  <.th(nls("Expected", "Verwacht")),
                  <.th(nls("Actual", "Gevonden"))
                )
              ),
              <.tbody(
                failedChecks.toTagMod { check =>
                  <.tr(
                    <.td(context.gotoNode(check.nodeId, check.nodeName)),
                    <.td(check.expected),
                    <.td(check.actual)
                  )
                }
              )
            )
          )
        }
      }
    }

    private def nameMissing(fact: Option[NetworkNameMissing]): Seq[TagMod] = {
      fact.toSeq.map { networkNameMissing =>
        <.div(
          UiThick(
            <.span(
              nls("NetworkNameMissing", "NetwerkNaamOntbreekt")
            )
          )
        )
      }
    }
  }

}
