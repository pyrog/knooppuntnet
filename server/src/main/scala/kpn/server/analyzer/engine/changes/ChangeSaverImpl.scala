package kpn.server.analyzer.engine.changes

import kpn.server.repository.ChangeSetRepository
import org.springframework.stereotype.Component

@Component
class ChangeSaverImpl(
  changeSetRepository: ChangeSetRepository
) extends ChangeSaver {

  def save(context: ChangeSetContext): Unit = {

    if (context.changes.nonEmpty) {

      context.changes.networkChanges.foreach { networkChange =>
        changeSetRepository.saveNetworkChange(networkChange)
      }

      context.changes.networkInfoChanges.foreach { networkInfoChange =>
        changeSetRepository.saveNetworkInfoChange(networkInfoChange)
      }

      context.changes.routeChanges.foreach { routeChange =>
        changeSetRepository.saveRouteChange(routeChange)
      }

      context.changes.nodeChanges.foreach { nodeChange =>
        changeSetRepository.saveNodeChange(nodeChange)
      }

      val changeSetSummary = new ChangeSetSummaryBuilder().build(context)
      changeSetRepository.saveChangeSetSummary(changeSetSummary)
    }
  }
}
