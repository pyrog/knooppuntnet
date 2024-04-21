package kpn.server.repository

import kpn.api.common.common.Reference
import kpn.api.common.route.RouteMapInfo
import kpn.api.common.route.RouteNameInfo
import kpn.api.custom.Country
import kpn.api.custom.NetworkType
import kpn.core.doc.RouteDoc
import kpn.core.util.Log
import kpn.database.actions.routes.MongoQueryKnownRouteIds
import kpn.database.actions.routes.MongoQueryRouteCountry
import kpn.database.actions.routes.MongoQueryRouteElementIds
import kpn.database.actions.routes.MongoQueryRouteIds
import kpn.database.actions.routes.MongoQueryRouteMapInfo
import kpn.database.actions.routes.MongoQueryRouteNameInfo
import kpn.database.actions.routes.MongoQueryRouteNetworkReferences
import kpn.database.actions.routes.MongoQueryRouteTileInfo
import kpn.database.base.Database
import kpn.server.analyzer.engine.changes.changes.ReferencedElementIds
import kpn.server.analyzer.engine.tiles.domain.RouteTileInfo
import org.springframework.stereotype.Component

@Component
class RouteRepositoryImpl(database: Database) extends RouteRepository {

  private val log = Log(classOf[RouteRepositoryImpl])

  override def allRouteIds(): Seq[Long] = {
    database.routes.ids(log)
  }

  override def activeRouteIds(): Seq[Long] = {
    new MongoQueryRouteIds(database).execute(log).sorted
  }

  override def activeRouteElementIds(): Seq[ReferencedElementIds] = {
    new MongoQueryRouteElementIds(database).execute()
  }

  override def save(routeDoc: RouteDoc): Unit = {
    database.routes.save(routeDoc, log)
  }

  override def bulkSave(routeDocs: Seq[RouteDoc]): Unit = {
    database.routes.bulkSave(routeDocs, log)
  }

  override def findById(routeId: Long): Option[RouteDoc] = {
    database.routes.findById(routeId, log)
  }

  override def mapInfo(routeId: Long): Option[RouteMapInfo] = {
    new MongoQueryRouteMapInfo(database).execute(routeId, log)
  }

  override def nameInfo(routeId: Long): Option[RouteNameInfo] = {
    new MongoQueryRouteNameInfo(database).execute(routeId, log)
  }

  override def networkReferences(routeId: Long): Seq[Reference] = {
    new MongoQueryRouteNetworkReferences(database).execute(routeId, log)
  }

  override def filterKnown(routeIds: Set[Long]): Set[Long] = {
    new MongoQueryKnownRouteIds(database).execute(routeIds.toSeq, log).toSet
  }

  override def routeTileInfosByNetworkType(networkType: NetworkType): Seq[RouteTileInfo] = {
    new MongoQueryRouteTileInfo(database).findByNetworkType(networkType)
  }

  override def routeTileInfosById(routeId: Long): Option[RouteTileInfo] = {
    new MongoQueryRouteTileInfo(database).findById(routeId)
  }

  override def routeCountry(routeId: Long): Option[Country] = {
    new MongoQueryRouteCountry(database).execute(routeId)
  }
}
