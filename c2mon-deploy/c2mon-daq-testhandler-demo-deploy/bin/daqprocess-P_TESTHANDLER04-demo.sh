#!/bin/bash

# Startup script for demo version of a given TIM process (DAQ) 

#set home directory of script
SCRIPT=`readlink -f $(which $0)`
SCRIPTPATH=`dirname $SCRIPT`
DAQ_HOME=`dirname $SCRIPTPATH`

# Global variables

# Process name
PROCESS_NAME=P_TESTHANDLER04

# check if JAVA_HOME is set
if [ -z $JAVA_HOME ]; then
   # try to find java if not
   export JAVA="$(readlink -f $(which java))"
else
   export JAVA=$JAVA_HOME/jre/bin/java
fi

# Java
JVM_MEM="-Xms256m -Xmx256m"
JVM_OTHER_OPTS=()

DAQ_LOG_HOME=${DAQ_HOME}/log
DAQ_CONF_HOME=$DAQ_HOME/conf
C2MON_PROPERTIES_FILE=$DAQ_CONF_HOME/c2mon.properties

#log4j configuration file (uncomment log4j.xml for a rolling appender)
#LOG4J_CONF_FILE=$DAQ_HOME/conf/log4j.xml
LOG4J_CONF_FILE=$DAQ_HOME/conf/log4j-standardout.xml

# DAQ xml file
ADDITIONAL_PARAMS="${ADDITIONAL_PARAMS} -c ${DAQ_CONF_HOME}/local/${PROCESS_NAME}.xml"

CLASSPATH=`ls $DAQ_HOME/lib/*.jar | tr -s '\n' ':'`

# Execution
exec -a `basename $0` $JAVA -cp "$CLASSPATH" -Dc2mon.process.name="$PROCESS_NAME" -Dc2mon.log.dir="$DAQ_LOG_HOME" $JVM_MEM "${JVM_OTHER_OPTS[@]}" cern.c2mon.daq.common.startup.DaqStartup -c2monProperties ${C2MON_PROPERTIES_FILE} -log4j ${LOG4J_CONF_FILE} -daqConf ${DAQ_CONF_HOME}/daq.conf -processName ${PROCESS_NAME} ${ADDITIONAL_PARAMS}

