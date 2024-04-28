#!/usr/bin/env bash
nohup /kpn/java/bin/java \
  -Dname=poi-tile-update-tool \
  -Dlog4j.configurationFile=/kpn/scripts/conf/poi-tile-update-tool-log.xml \
  -Xms128M \
  -Xmx512M \
  -Dcom.sun.management.jmxremote.port=5104 \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false \
  -cp /kpn/bin/server.jar \
  -Dloader.main=kpn.core.tools.poi.PoiTileUpdateTool org.springframework.boot.loader.launch.PropertiesLauncher \
  --tile-dir /kpn/tiles \
  --host kpn-web \
  --poi-database kpn-prod \
  --task-database kpn-prod \
  > /kpn/logs/poi-tile-update-tool-stdout.log 2>&1 &
