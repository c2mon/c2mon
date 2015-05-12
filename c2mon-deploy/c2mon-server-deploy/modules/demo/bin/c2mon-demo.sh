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

# check if JAVA_HOME is set
if [ -z $JAVA_HOME ]; then
   # try to find java if not
   export JAVA="$(readlink -f $(which java))"
else
   export JAVA=$JAVA_HOME/jre/bin/java
fi

#.c2mon.properties location
C2MON_PROPERTIES=$C2MON_HOME/conf/c2mon.properties

#################
# COMMON SETUP  #
#################

# Process name
PROCESS_NAME=C2MON-TIM-PRO1

#log4j configuration file
LOG4J_CONF_FILE=$C2MON_HOME/conf/log4j.xml

#log directory (not used unless log4j config is changed)
LOG_DIR=$C2MON_HOME/log

#######################
# start/stop commands #
#######################

CLASSPATH=`ls $C2MON_HOME/lib/*.jar | tr -s '\n' ':'`


HSQLDB_NAME=stl
HSQLDB_START_CMD="nohup $JAVA -cp "${CLASSPATH}" org.hsqldb.Server -database.0 file:$HSQLDB_NAME -dbname.0 $HSQLDB_NAME"

# trap ctrl-c
trap ctrl_c INT

function ctrl_c() {
  echo "Trapped CTRL-C"
  # stop hsqldb
  kill $(ps aux | grep 'hsqldb' | awk '{print $2}')
}

# start hsqldb
echo "Starting local HSQLDB instance..."
mkdir -p $C2MON_HOME/hsqldb
cd $C2MON_HOME/hsqldb
$HSQLDB_START_CMD >/dev/null 2>&1 &
cd -


COMMON_JAVA_ARGS="-Xms512m -Xmx512m -XX:NewRatio=3 -XX:+UseParallelGC -XX:MaxGCPauseMillis=100 -Dserver.process.name=$PROCESS_NAME -Dc2mon.process.name=$PROCESS_NAME -Dc2mon.home=$C2MON_HOME -Dlog4j.configuration=$LOG4J_CONF_FILE -Dc2mon.log.dir=$LOG_DIR -Dc2mon.properties.location=$C2MON_PROPERTIES -Dcern.c2mon.cache.mode=single-nonpersistent"

#uncomment for recovery start
#C2MON_RECOVERY_ARG="-Dc2mon.recovery=true"

C2MON_JAVA_ARGS="$COMMON_JAVA_ARGS $C2MON_RECOVERY_ARG"
C2MON_START_CMD="$JAVA $C2MON_JAVA_ARGS -cp "${CLASSPATH}" cern.c2mon.server.lifecycle.ServerStartup $C2MON_ARGS"

echo "Starting C2MON..."
$C2MON_START_CMD >$LOG_DIR/c2mon.stdout.log 2> $LOG_DIR/c2mon.stderr.log &
