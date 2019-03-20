package kpn.core.planner

import kpn.core.planner.graph.NodeNetworkGraph
import kpn.core.planner.graph.NodeNetworkGraphImpl
import kpn.core.repository.GraphRepository
import kpn.core.repository.NodeRepository
import kpn.core.repository.RouteRepository
import kpn.shared.NetworkType

class PlannerFacadeImpl(
  graphRepository: GraphRepository,
  nodeRepository: NodeRepository,
  routeRepository: RouteRepository
) extends PlannerFacade {

  private val bicyclePlanUpdater = new PlanUpdater(buildGraph(NetworkType.bicycle))
  private val hikingPlanUpdater = new PlanUpdater(buildGraph(NetworkType.hiking))
  private val planBuilder = new PlanBuilder(nodeRepository, routeRepository)

  override def add(networkType: NetworkType, encodedPlanString: String, nodeId: Long): Plan = {
    plan(networkType, encodedPlanString) { (planUpdater, encodedPlan) =>
      planUpdater.add(encodedPlan, nodeId)
    }
  }

  override def undo(networkType: NetworkType, encodedPlanString: String): Plan = {
    plan(networkType, encodedPlanString) { (planUpdater, encodedPlan) =>
      planUpdater.undo(encodedPlan)
    }
  }

  override def reset(networkType: NetworkType, encodedPlanString: String): Plan = {
    plan(networkType, encodedPlanString) { (planUpdater, encodedPlan) =>
      planUpdater.reset(encodedPlan)
    }
  }

  private def plan(networkType: NetworkType, encodedPlanString: String)(f: (PlanUpdater, EncodedPlan) => EncodedPlan): Plan = {
    val encodedPlan: EncodedPlan = EncodedPlan(encodedPlanString)
    val updatedEncodedPlan: EncodedPlan = networkType match {
      case NetworkType.bicycle => f(bicyclePlanUpdater, encodedPlan)
      case NetworkType.hiking => f(hikingPlanUpdater, encodedPlan)
    }
    planBuilder.build(networkType, updatedEncodedPlan)
  }

  private def buildGraph(networkType: NetworkType): NodeNetworkGraph = {
    val graph = new NodeNetworkGraphImpl()
    val edges = graphRepository.edges(networkType)
    edges.foreach(graph.add)
    graph
  }
}
