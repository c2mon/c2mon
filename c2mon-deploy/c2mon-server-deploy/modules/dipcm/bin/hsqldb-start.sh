#!/bin/bash
#
# Startup script for HSQL DB
# JAVA_HOME must be set beforehand.

########################
# DEPLOYMENT VARIABLES #
########################

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

#######################
# start/stop commands #
#######################

HSQLDB_NAME=stl
HSQLDB_START_CMD="nohup $JAVA -cp "$C2MON_HOME/lib/hsqldb-2.3.2.jar" org.hsqldb.Server -database.0 file:$HSQLDB_NAME -dbname.0 $HSQLDB_NAME"

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
$HSQLDB_START_CMD >/dev/null 2>&1

