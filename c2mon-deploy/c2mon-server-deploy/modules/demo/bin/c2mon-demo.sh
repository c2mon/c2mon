#!/bin/bash
#
# Startup script for demo version of the C2MON server.
# JAVA_HOME must be set beforehand.

########################
# DEPLOYMENT VARIABLES #
########################

#start a single non-clustered server

# get the current location
SCRIPT=`readlink -f $(which $0)`
SCRIPTPATH=`dirname $SCRIPT`
C2MON_HOME=$SCRIPTPATH/..

# make sure JAVA_HOME is set correctly
if [ -z $JAVA_HOME ]; then
   # use default if not
   export JAVA_HOME=/usr/java/jdk
fi

#.c2mon.properties location
C2MON_PROPERTIES=$C2MON_HOME/conf/c2mon.properties

#################
# COMMON SETUP  #
#################

# Process name
PROCESS_NAME=C2MON-TIM-PRO1

#log4j configuration file (uncomment log4j.xml for a rolling appender)
#LOG4J_CONF_FILE=$C2MON_HOME/conf/log4j.xml
LOG4J_CONF_FILE=$C2MON_HOME/conf/log4j-standardout.xml
#log directory (not used unless log4j config is changed)
LOG_DIR=$C2MON_HOME/log

#######################
# start/stop commands #
#######################

CLASSPATH=`ls $C2MON_HOME/lib/*.jar | tr -s '\n' ':'`

COMMON_JAVA_ARGS="-Xms512m -Xmx512m -XX:NewRatio=3 -XX:+PrintGCDetails -XX:+UseParallelGC -XX:MaxGCPauseMillis=100 -Dserver.process.name=$PROCESS_NAME -Dc2mon.process.name=$PROCESS_NAME -Dc2mon.home=$C2MON_HOME -Dlog4j.configuration=$LOG4J_CONF_FILE -Dc2mon.log.dir=$LOG_DIR -Dc2mon.properties.location=$C2MON_PROPERTIES"

CLUSTER_JAVA_ARGS="-Dcom.tc.l1.cachemanager.percentageToEvict=10 -Dcom.tc.l1.cachemanager.threshold=70 -Dcom.tc.l1.cachemanager.monitorOldGenOnly=false -Dterracotta.config.location=$TC_CONFIG_PATH $CACHE_MODE_PROPERTY -Dcom.tc.productkey.path=$C2MON_HOME/conf/terracotta-license.key"

#uncomment for recovery start
#C2MON_RECOVERY_ARG="-Dc2mon.recovery=true"

C2MON_JAVA_ARGS="$COMMON_JAVA_ARGS $CLUSTER_JAVA_ARGS $C2MON_RECOVERY_ARG"
C2MON_START_CMD="$JAVA_HOME/jre/bin/java $C2MON_JAVA_ARGS -cp "${CLASSPATH}" cern.c2mon.server.lifecycle.ServerStartup $C2MON_ARGS"

$C2MON_START_CMD
