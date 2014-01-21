#!/bin/bash
#
# Startup script for demo version of the C2MON server.
# JAVA_HOME must be set beforehand.

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

#log4j configuration file
LOG4J_CONF_FILE=$C2MON_HOME/conf/log4j.xml
#log directory (not used unless log4j config is changed)
LOG_DIR=$C2MON_HOME/log

CLASSPATH=`ls $C2MON_HOME/lib/*.jar | tr -s '\n' ':'`

COMMON_JAVA_ARGS="-Xms512m -Xmx512m -XX:NewRatio=3 -XX:+PrintGCDetails -XX:+UseParallelGC -XX:MaxGCPauseMillis=100 -Dserver.process.name=$PROCESS_NAME -Dc2mon.process.name=$PROCESS_NAME -Dc2mon.home=$C2MON_HOME -Dlog4j.configuration=$LOG4J_CONF_FILE -Dc2mon.log.dir=$LOG_DIR -Dc2mon.properties.location=$C2MON_PROPERTIES"

#uncomment for recovery start
#C2MON_RECOVERY_ARG="-Dc2mon.recovery=true"

C2MON_JAVA_ARGS="$COMMON_JAVA_ARGS $C2MON_RECOVERY_ARG"
C2MON_START_CMD="$JAVA_HOME/jre/bin/java $C2MON_JAVA_ARGS -cp "${CLASSPATH}" cern.c2mon.server.lifecycle.ServerStartup $C2MON_ARGS"

$C2MON_START_CMD 
