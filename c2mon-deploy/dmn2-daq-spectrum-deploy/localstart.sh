#!/bin/bash
cd deploy

PARAMS="\
-Dc2mon.process.name=P_SPECTRUM \
-Dc2mon.log.dir=log \
-Dc2mon.log.filename=spectrum \
-Dapp.name=c2mon_spectrum_monitor \
-Dapp.version=0.1 \
-Dc2mon.daq.spring.context=file:conf/daq-core-service.xml \
"

CMDLINE="\
-c2monProperties conf/daq.properties -log4j log4j.xml -daqConf conf/daq.conf -processName P_SPECTRUM -noDeadband \
"

pwd
java ${PARAMS} -cp "../../c2mon-daq-spectrum/build/classes:lib/*:." cern.c2mon.daq.common.startup.DaqStartup ${CMDLINE}

