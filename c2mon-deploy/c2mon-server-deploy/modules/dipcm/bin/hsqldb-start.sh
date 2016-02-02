#!/bin/bash
#
# Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
# 
# This file is part of the CERN Control and Monitoring Platform 'C2MON'.
# C2MON is free software: you can redistribute it and/or modify it under the
# terms of the GNU Lesser General Public License as published by the Free
# Software Foundation, either version 3 of the license.
# 
# C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
# more details.
# 
# You should have received a copy of the GNU Lesser General Public License
# along with C2MON. If not, see <http://www.gnu.org/licenses/>.
##

# Startup script for HSQLDB

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

