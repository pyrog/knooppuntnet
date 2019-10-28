package kpn.server.repository

import kpn.core.database.Database
import kpn.core.database.views.location.LocationDesign
import kpn.core.database.views.location.LocationView
import kpn.shared.NetworkType
import kpn.shared.common.Ref
import org.springframework.stereotype.Component

@Component
class LocationRepositoryImpl(analysisDatabase: Database) extends LocationRepository {

  override def routesWithoutLocation(networkType: NetworkType): Seq[Ref] = {
    val rows = analysisDatabase.old.query(LocationDesign, LocationView)("route-without-location", networkType.newName)
    rows.map(LocationView.toRef).sortBy(_.name)
  }

}
