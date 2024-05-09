package kpn.api.common.changes.filter

case class ServerFilterGroup(
  name: String,
  options: Seq[ServerFilterOption]
)
