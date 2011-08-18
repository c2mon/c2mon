#!/bin/bash
#

##############################################################
# Startscript for C2MON server app
#
#


# sets the home directory
# if we are in /opt/test/bin/  (executables should be in bin/.)
# then HOME=/opt/test
#
HOME=`dirname $0`
[[ $HOME == "." ]] && HOME=$PWD
HOME=$HOME/..

C2MON_HOME=$HOME
C2MON_LOG=$C2MON_HOME/log
C2MON_LIB=$C2MON_HOME/lib
C2MON_OPTIONAL_LIB=$C2MON_LIB/optional
C2MON_ARGS=
JAVA_EXEC=/usr/java/jdk/bin/java


# if not set externally, start as the script name
[[ -z $PROCESS_NAME ]] && PROCESS_NAME=`basename $0`

# .c2mon.properties location
C2MON_PROPERTIES=$C2MON_HOME/conf/.c2mon.properties

# log4j configuration file
C2MON_LOG4J=$C2MON_HOME/conf/log4j.xml


####################
# Terracotta module libraries
#
TERRACOTTA_HOME=
TC_MOD_HOME=$TERRACOTTA_HOME/platform/modules/org/terracotta/modules
TERRACOTTA_JARS=$TC_MOD_HOME/tim-distributed-cache/1.3.2/tim-distributed-cache-1.3.2.jar:$TC_MOD_HOME/tim-ehcache-2.0/1.5.2/tim-ehcache-2.0-1.5.2.jar:$TC_MOD_HOME/tim-concurrent-collections/1.3.2/tim-concurrent-collections-1.3.2.jar:$TC_MOD_HOME/tim-async-processing/1.3.2/tim-async-processing-1.3.2.jar:$TC_MOD_HOME/tim-annotations/1.5.1/tim-annotations-1.5.1.jar
TC_PORT=9510
TC_HOST=cs-ccr-tim4
TERRACOTTA_CONFIG=$TC_HOST:$TC_PORT
####################


JVM_OTHER_OPTS=""
JARS=$C2MON_LIB/*
JARS=`ls $C2MON_LIB/*.jar | tr -s '\\n' ':'`



# Common JVM arguments
#
COMMON_JAVA_ARGS="-Xms2048m -Xmx2048m -XX:+PrintGCDetails -XX:+UseParallelGC -XX:MaxGCPauseMillis=100 \
                -Dserver.process.name=$PROCESS_NAME \
                -Dc2mon.home=$C2MON_HOME \
                -Dc2mon.log.dir=$C2MON_LOG \
                -Dc2mon.properties.location=$C2MON_PROPERTIES
                -Dlog4j.configuration=$C2MON_LOG4J \
                -Dcom.sun.management.jmxremote.port=9523 \
                -Dcom.sun.management.jmxremote.password.file=$C2MON_HOME/conf/.jmxremote.passwd \
                -Dcom.sun.management.jmxremote.access.file=$C2MON_HOME/conf/.jmxremote.access \
                -Dcom.sun.management.jmxremote.ssl=false"


# Run in cluster mode
#
if [ "$2" != "single" ]; then
    CLUSTER_JAVA_ARGS="-Dcom.tc.l1.cachemanager.percentageToEvict=10 \
                   -Dcom.tc.l1.cachemanager.threshold=70 \
                   -Dcom.tc.l1.cachemanager.monitorOldGenOnly=false \
                   -Dtc.config=$TERRACOTTA_CONFIG \
                   -Dcern.c2mon.cache.mode=multi"

    JARS=$JARS:$TERRACOTTA_JARS
    JVM_OPTS="$COMMON_JAVA_ARGS CLUSTER_JAVA_ARGS"
    JAVA_EXEC=$TERRACOTTA_HOME/bin/dso-java.sh
else
    JVM_OPTS=$COMMON_JAVA_ARGS
fi


# Execution of the real program
#
cd $C2MON_HOME
exec -a $PROCESS_NAME $JAVA_EXEC -classpath $JARS $JVM_OPTS cern.tim.server.lifecycle.ServerStartup $C2MON_ARGS
