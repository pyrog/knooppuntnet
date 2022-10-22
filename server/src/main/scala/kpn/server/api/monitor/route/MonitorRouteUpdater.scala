package kpn.server.api.monitor.route

import kpn.api.base.ObjectId
import kpn.api.common.Bounds
import kpn.api.common.monitor.MonitorRouteProperties
import kpn.api.common.monitor.MonitorRouteSaveResult
import kpn.api.custom.Day
import kpn.api.custom.Timestamp
import kpn.core.common.Time
import kpn.core.overpass.OverpassQueryExecutorRemoteImpl
import kpn.core.util.Log
import kpn.database.util.Mongo
import kpn.server.analyzer.engine.monitor.MonitorRouteAnalysisSupport
import kpn.server.analyzer.engine.monitor.MonitorRouteAnalyzer
import kpn.server.analyzer.engine.monitor.MonitorRouteAnalyzerImpl
import kpn.server.api.monitor.domain.MonitorGroup
import kpn.server.api.monitor.domain.MonitorRoute
import kpn.server.api.monitor.domain.MonitorRouteReference
import kpn.server.repository.MonitorGroupRepository
import kpn.server.repository.MonitorGroupRepositoryImpl
import kpn.server.repository.MonitorRouteRepository
import kpn.server.repository.MonitorRouteRepositoryImpl
import org.locationtech.jts.geom.GeometryCollection
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.io.geojson.GeoJsonWriter
import org.springframework.stereotype.Component

object MonitorRouteUpdater {

  def main(args: Array[String]): Unit = {
    Mongo.executeIn("kpn-experimental") { database =>
      val monitorGroupRepository = new MonitorGroupRepositoryImpl(database)
      val monitorRouteRepository = new MonitorRouteRepositoryImpl(database)
      val overpassQueryExecutor = new OverpassQueryExecutorRemoteImpl()
      val monitorRouteRelationRepository = new MonitorRouteRelationRepository(overpassQueryExecutor)
      val monitorRouteAnalyzer = new MonitorRouteAnalyzerImpl(monitorRouteRepository, overpassQueryExecutor)
      val updater = new MonitorRouteUpdater(
        monitorGroupRepository,
        monitorRouteRepository,
        monitorRouteRelationRepository,
        monitorRouteAnalyzer
      )
      updater.analyze("A", "a")
    }
  }
}

@Component
class MonitorRouteUpdater(
  monitorGroupRepository: MonitorGroupRepository,
  monitorRouteRepository: MonitorRouteRepository,
  monitorRouteRelationRepository: MonitorRouteRelationRepository,
  monitorRouteAnalyzer: MonitorRouteAnalyzer
) {

  def add(user: String, groupName: String, properties: MonitorRouteProperties): MonitorRouteSaveResult = {
    Log.context(Seq("add-route", s"group=$groupName", s"route=${properties.name}")) {
      val group = findGroup(groupName)
      assertNewRoute(group, properties.name)
      val relationId = properties.relationId
      val route = MonitorRoute(
        ObjectId(),
        group._id,
        properties.name,
        properties.description,
        relationId,
      )
      monitorRouteRepository.saveRoute(route)

      if (properties.referenceType == "osm") {
        updateOsmReference(user, route, properties)
      }
      else {
        // the route reference will be created at the time the gpxfile is uploaded
        // the route analysis will be done in a separate call, after the gpx file has been uploaded
        MonitorRouteSaveResult()
      }
    }
  }

  def update(user: String, groupName: String, routeName: String, properties: MonitorRouteProperties): MonitorRouteSaveResult = {
    Log.context(Seq("route-update", s"group=$groupName", s"route=$routeName")) {
      val group = findGroup(groupName)
      val route = findRoute(group._id, routeName)
      val reference = findRouteReference(route._id)

      if (isRouteChanged(group, route, properties)) {
        val groupId = if (group.name != properties.groupName) {
          val newGroup = findGroup(properties.groupName)
          newGroup._id
        }
        else {
          route.groupId
        }
        monitorRouteRepository.saveRoute(
          route.copy(
            groupId = groupId,
            name = properties.name,
            description = properties.description,
            relationId = properties.relationId
          )
        )
      }

      if (properties.referenceType == "osm") {
        if (isOsmReferenceChanged(reference, properties) || isRelationIdChanged(route, properties)) {
          val updatedRoute = if (isRelationIdChanged(route, properties)) {
            route.copy(
              relationId = properties.relationId
            )
          }
          else {
            route
          }
          updateOsmReference(user, updatedRoute, properties)
          MonitorRouteSaveResult(analyzed = true)
        }
        else {
          MonitorRouteSaveResult()
        }
      }
      else if (properties.referenceType == "gpx") {
        if (properties.gpxFileChanged) {
          // reference has changed, but details will arrive in next api call
          // re-analyze only after reference has been updated
          MonitorRouteSaveResult()
        }
        else if (isRelationIdChanged(route, properties)) {
          // reference does not change, but we have re-analyze because the relationId has changed
          monitorRouteAnalyzer.analyze(route, reference)
          MonitorRouteSaveResult(analyzed = true)
        }
        else {
          MonitorRouteSaveResult()
        }
      }
      else {
        MonitorRouteSaveResult()
      }
    }
  }

  def analyze(groupName: String, routeName: String): Unit = {
    Log.context(Seq("route-analyze", s"group=$groupName", s"route=$routeName")) {
      val group = findGroup(groupName)
      val route = findRoute(group._id, routeName)
      val reference = findRouteReference(route._id)
      monitorRouteAnalyzer.analyze(route, reference)
    }
  }

  private def updateOsmReference(user: String, route: MonitorRoute, properties: MonitorRouteProperties): MonitorRouteSaveResult = {

    properties.relationId match {
      case None =>
        val reference = MonitorRouteReference(
          ObjectId(),
          routeId = route._id,
          relationId = None,
          created = Time.now,
          user = user,
          bounds = Bounds(),
          referenceType = "osm",
          osmReferenceDay = properties.osmReferenceDay,
          segmentCount = 0,
          filename = None,
          geometry = ""
        )
        monitorRouteRepository.saveRouteReference(reference)
        MonitorRouteSaveResult(errors = Seq("no-relation-id"))

      case Some(relationId) =>

        val osmReferenceDay = findOsmReferenceDay(properties)

        monitorRouteRelationRepository.load(Timestamp(osmReferenceDay), relationId) match {
          case None =>
            val reference = MonitorRouteReference(
              ObjectId(),
              routeId = route._id,
              relationId = properties.relationId,
              created = Time.now,
              user = user,
              bounds = Bounds(),
              referenceType = "osm",
              osmReferenceDay = properties.osmReferenceDay,
              segmentCount = 0,
              filename = None,
              geometry = ""
            )
            monitorRouteRepository.saveRouteReference(reference)
            MonitorRouteSaveResult(errors = Seq("osm-relation-not-found"))

          case Some(relation) =>
            val nodes = relation.wayMembers.flatMap(_.way.nodes)
            val bounds = Bounds.from(nodes)
            val routeSegments = MonitorRouteAnalysisSupport.toRouteSegments(relation)
            val geomFactory = new GeometryFactory
            val geometryCollection = new GeometryCollection(routeSegments.map(_.lineString).toArray, geomFactory)
            val geoJsonWriter = new GeoJsonWriter()
            geoJsonWriter.setEncodeCRS(false)
            val geometry = geoJsonWriter.write(geometryCollection)

            val reference = MonitorRouteReference(
              ObjectId(),
              routeId = route._id,
              relationId = properties.relationId,
              created = Time.now,
              user = user,
              bounds = bounds,
              referenceType = "osm",
              osmReferenceDay = properties.osmReferenceDay,
              segmentCount = routeSegments.size,
              filename = None,
              geometry = geometry
            )
            monitorRouteRepository.saveRouteReference(reference)
            monitorRouteAnalyzer.analyze(route, reference)
            MonitorRouteSaveResult(analyzed = true)
        }
    }
  }

  private def findGroup(groupName: String): MonitorGroup = {
    monitorGroupRepository.groupByName(groupName).getOrElse {
      throw new IllegalArgumentException(
        s"""${Log.contextString} Could not find group with name "$groupName""""
      )
    }
  }

  private def findRoute(groupId: ObjectId, routeName: String): MonitorRoute = {
    monitorRouteRepository.routeByName(groupId, routeName).getOrElse {
      throw new IllegalArgumentException(
        s"""${Log.contextString} Could not find route with name "$routeName" in group "${groupId.oid}""""
      )
    }
  }

  private def findRouteReference(routeId: ObjectId): MonitorRouteReference = {
    monitorRouteRepository.routeReferenceRouteWithId(routeId).getOrElse {
      throw new IllegalArgumentException(
        s"""${Log.contextString} Could not find reference for route with id "$routeId""""
      )
    }
  }

  private def findRelationId(properties: MonitorRouteProperties): Long = {
    properties.relationId.getOrElse {
      throw new IllegalArgumentException(s"""${Log.contextString} relationId is required when add route with referenceType="osm"""")
    }
  }

  private def findOsmReferenceDay(properties: MonitorRouteProperties): Day = {
    properties.osmReferenceDay.getOrElse {
      throw new IllegalArgumentException(s"""${Log.contextString} osmReferenceDay is required in route with referenceType="osm"""")
    }
  }

  private def assertNewRoute(group: MonitorGroup, routeName: String): Unit = {
    monitorRouteRepository.routeByName(group._id, routeName) match {
      case None =>
      case Some(route) =>
        throw new IllegalArgumentException(
          s"""${Log.contextString} Could not add route with name "$routeName": already exists (_id=${route._id.oid}) in group with name "${group.name}""""
        )
    }
  }

  private def isRouteChanged(group: MonitorGroup, route: MonitorRoute, properties: MonitorRouteProperties): Boolean = {
    route.name != properties.name ||
      route.description != properties.description ||
      route.relationId != properties.relationId ||
      group.name != properties.groupName
  }

  private def isOsmReferenceChanged(reference: MonitorRouteReference, properties: MonitorRouteProperties): Boolean = {
    reference.referenceType != properties.referenceType ||
      reference.relationId != properties.relationId ||
      reference.osmReferenceDay != properties.osmReferenceDay
  }

  private def isRelationIdChanged(route: MonitorRoute, properties: MonitorRouteProperties): Boolean = {
    route.relationId != properties.relationId
  }
}