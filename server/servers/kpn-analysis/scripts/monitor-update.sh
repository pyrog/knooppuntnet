#!/usr/bin/env bash
nohup /kpn/java/bin/java \
  -Dname=monitor-update-tool \
  -Dlog4j.configurationFile=/kpn/scripts/conf/monitor-update-tool-log.xml \
  -Xms1024M \
  -Xmx6G \
  -Xss128M \
  -cp /kpn/bin/server-experimental.jar \
  -Dloader.main=kpn.core.tools.monitor.MonitorDemoUpdateTool org.springframework.boot.loader.PropertiesLauncher \
  -d kpn >> /kpn/logs/monitor-update-tool-stdout.log 2>&1 &