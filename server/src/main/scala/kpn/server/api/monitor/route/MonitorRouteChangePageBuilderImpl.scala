package kpn.server.api.monitor.route

import kpn.api.common.BoundsI
import kpn.api.common.monitor.MonitorRouteChangePage
import kpn.api.common.monitor.MonitorRouteReferenceInfo
import kpn.server.repository.ChangeSetInfoRepository
import kpn.server.repository.MonitorGroupRepository
import kpn.server.repository.MonitorRouteRepository
import org.springframework.stereotype.Component

@Component
class MonitorRouteChangePageBuilderImpl(
  monitorRouteRepository: MonitorRouteRepository,
  monitorGroupRepository: MonitorGroupRepository,
  changeSetInfoRepository: ChangeSetInfoRepository
) extends MonitorRouteChangePageBuilder {

  override def build(routeId: Long, changeSetId: Long, replicationId: Long): Option[MonitorRouteChangePage] = {

    val comment = changeSetInfoRepository.get(changeSetId).flatMap(_.tags("comment"))

    monitorRouteRepository.routeChange(routeId, changeSetId, replicationId).flatMap { routeChange =>
      //      monitorRouteRepository.routeChangeGeometry(routeId, changeSetId, replicationId).map { routeChangeGeometry =>
      monitorGroupRepository.group(routeChange.groupName).map { group =>
        val routeReference = routeChange.reference.flatMap { reference =>
          monitorRouteRepository.routeReference(routeId, reference.key).map { routeReference =>
            MonitorRouteReferenceInfo(
              routeReference.key,
              routeReference.created,
              routeReference.user,
              routeReference.bounds,
              0, // TODO distance
              routeReference.referenceType,
              routeReference.referenceTimestamp,
              routeReference.segmentCount,
              routeReference.filename,
              routeReference.geometry
            )
          }
        }

        MonitorRouteChangePage(
          routeChange.key,
          group.name,
          group.description,
          comment,
          routeChange.wayCount,
          routeChange.waysAdded,
          routeChange.waysRemoved,
          routeChange.waysUpdated,
          routeChange.osmDistance,
          BoundsI(), // TODO routeChangeGeometry.bounds,
          routeChange.routeSegmentCount,
          Seq.empty, // TODO routeChangeGeometry.routeSegments,
          Seq.empty, // TODO routeChangeGeometry.newNokSegments,
          Seq.empty, // TODO routeChangeGeometry.resolvedNokSegments,
          routeReference,
          routeChange.happy,
          routeChange.investigate
        )
      }
    }
  }
}