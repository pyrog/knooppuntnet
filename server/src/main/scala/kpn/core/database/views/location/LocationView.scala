package kpn.core.database.views.location

import kpn.api.common.common.Ref
import kpn.api.custom.NetworkType
import kpn.core.database.Database
import kpn.core.database.query.Fields
import kpn.core.database.query.Query
import kpn.core.database.views.common.View

object LocationView extends View {

  private case class ViewResultRow(value: Seq[String])

  private case class ViewResult(rows: Seq[ViewResultRow])

  def query(database: Database, elementType: String, networkType: NetworkType, locationName: String): Seq[Ref] = {

    val query = Query(LocationDesign, LocationView, classOf[ViewResult])
      .keyStartsWith(elementType, networkType.name, locationName)
      .reduce(false)

    val result = database.execute(query)
    result.rows.map { row =>
      val fields = Fields(row.value)
      Ref(
        id = fields.long(1),
        name = fields.string(0)
      )
    }
  }

  override val reduce: Option[String] = Some("_count")
}
