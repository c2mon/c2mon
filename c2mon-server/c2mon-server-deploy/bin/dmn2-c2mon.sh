#!/bin/bash
#
# Description: Starts and stops the C2MON server. Can also be used
# to manage a cluster of two C2MON servers, with options for stopping
# and starting a second C2MON server on a different machine. 
#
# When deploying, adjust the variables in the first two sections
# below.
#
# HOME variable must be set before calling this script.

########################
# DEPLOYMENT VARIABLES #
########################

#export Java for TC script also!
export JAVA_HOME=/usr/java/jdk1.6.0_11
#server home
C2MON_HOME=$HOME/dev/tim2-prototype
#.tim.properties location
TIM_PROPERTIES=$HOME/.tim2.properties
#first C2MON host (must always be set; in non-clustered mode, the server will be started on this machine)
C2MON_PRIMARY_HOST=cs-ccr-tim3

##########################################
# DEPLOYMENT VARIABLES: CLUSTERED CONFIG #
##########################################

#Terracotta server host (must be set if running server cluster)
TC_HOST=cs-ccr-tim4
#Terracotta installation directory
TERRACOTTA_HOME=$HOME/opt/terracotta
#second C2MON host (must be set if running server cluster)
C2MON_SECOND_HOST=cs-ccr-tim8
#terracotta server name (one of them)
#TC_NAME=server1
#terracotta DSO port
TC_PORT=9510

#################
# COMMON SETUP  #
#################

#config home
CONF_HOME=$C2MON_HOME/config
#log4j configuration file
LOG4J_CONF_FILE=$CONF_HOME/log4j.xml
#log directory
LOG_DIR=$C2MON_HOME/log
#directory of shared libraries
SHARED_LIB_HOME=$HOME/dist/libs

###################
# CLUSTERED SETUP #
###################

#Terracotta configuration location (either file or host:port)
TERRACOTTA_CONFIG=$TC_HOST:$TC_PORT

####################
# MORE SETTINGS... #
####################

#set correct host
if [ "$2" == "second" ] ; then
  C2MON_HOST=$C2MON_SECOND_HOST
else
  C2MON_HOST=$C2MON_PRIMARY_HOST
fi

HOST_TMP_DIR=$C2MON_HOME/tmp/$C2MON_HOST
if [ ! -d "$HOST_TMP_DIR" ]; then
    mkdir $HOST_TMP_DIR
fi
  
#set correct PID file
C2MON_PIDFILE=$HOST_TMP_DIR/c2mon.pid

# jmx admin user name for shutdown
# !!! before production, move these to external script in home directory !!!
JMX_USER=timjmx
# jmx admin password
JMX_PASSWORD=jmxtim
# server port
JMX_PORT=9523

# The JAPC device name were the heartbeat is published on.
# This variable must be different in TEST and Operation to
# avoid mixing the heartbeat messages.
# Default is TIM_DEVICE
#JAPC_DEVICE_NAME=TIM_TEST_DEVICE

# Shortterm fallback log file for datatags. This is used in case the db connection is broken.
#TIM_LOG_FALLBACK_FILE=./timlog/fallback/ShortTermLogFallback.log

# Shortterm fallback log file for commandTags. This is used in case the db connection is broken
#TIM_LOG_COMMAND_FALLBACK_FILE=./timlog/fallback/ShortTermLogCommandFallback.log

# Email address for notifications sent out by the silentcheck function
#NOTIFY=tim.support@cern.ch

# SSH user name for copying files from the shared directory structure
#SSH_USER=timtest

#if [ "$USER" == "root" ]; then
#  echo "The OC4J service must not be started as root. Use the script oc4.service instead."
#  exit 1
#fi

##############
# LIBRAIRIES #
##############

#librairies (links to required libs)
APP_LIB_DIR=$C2MON_HOME/libs
APP_LIBS=$APP_LIB_DIR/*

#Terracotta module libraries
TC_MOD_HOME=$TERRACOTTA_HOME/platform/modules/org/terracotta/modules
TERRACOTTA_MODULES=$TC_MOD_HOME/tim-distributed-cache/1.3.2/tim-distributed-cache-1.3.2.jar:$TC_MOD_HOME/tim-ehcache-2.0/1.5.2/tim-ehcache-2.0-1.5.2.jar:$TC_MOD_HOME/tim-concurrent-collections/1.3.2/tim-concurrent-collections-1.3.2.jar:$TC_MOD_HOME/tim-async-processing/1.3.2/tim-async-processing-1.3.2.jar:$TC_MOD_HOME/tim-annotations/1.5.1/tim-annotations-1.5.1.jar

#Spring modules
SPRING_MODULES=$SHARED_LIB_HOME/spring-modules/spring-modules-cache

#libs for Ehcache
#EHCACHE_LIBS=$SHARED_LIB_HOME/ehcache/ehcache-core-2.0.1.jar:$SHARED_LIB_HOME/slf4j/slf4j-api-1.5.8.jar:$SHARED_LIB_HOME/slf4j/slf4j-jdk14-1.5.8.jar
#$SHARED_LIB_HOME/ehcache/ehcache-terracotta-2.0.1.jar:

#other libs
#OTHER_LIBS=$SHARED_LIB_HOME/jta/jta-1.1.jar:$SHARED_LIB_HOME/fuse/jencks-amqpool-2.2.jar:$SHARED_LIB_HOME/jencks/jencks-2.1.jar:$SHARED_LIB_HOME/apache-activemq/activemq-ra-5.3.0.jar

#apache commons libs
#APACHE_COMMONS_LIBS=$SHARED_LIB_HOME/apache-commons/commons-cli-1.1.jar:$SHARED_LIB_HOME/apache-commons/commons-io-1.4.jar

#application jar location
APPLICATION_JAR=$C2MON_HOME/dist/c2mon.jar

#all needed librairies (including cachetest)
if [ ! "$2" == "single" ]; then
    REQUIRED_LIBS=$APPLICATION_JAR:$APP_LIBS:$TERRACOTTA_MODULES:$SPRING_MODULES
else
    REQUIRED_LIBS=$APPLICATION_JAR:$APP_LIBS
fi

#:$EHCACHE_LIBS
#echo required libs: $REQUIRED_LIBS

#######################
# start/stop commands #
#######################

#OC4J_ARGS="-verbosity 9 -userThreads -out timlog/out.log -err timlog/err.log -config timconfig/server.xml "
C2MON_ARGS=

#OC4J_JAVA_ARGS="-server -Xmx1024m -Xms1024m -DKeepWrapperCode=false -Drmi.debug=true -Drmi.verbose=true -DDBEntityObjectDebug=false -Djdbc.debug=false -Doracle.mdb.fastUndeploy=30 -Ddatasource.verbose=false  -Doracle.jdbc.LogFile=timlog/jdbc.log -Doracle.ias.jcache=true -Doracle.dms.sensors=none -Doracle.jdbc.Trace=false -Dtim.log.file=./timlog/tim.log -Dtim.log.fallback.file=$TIM_LOG_FALLBACK_FILE -Dtim.log.fallback.counter.file=$TIM_LOG_FALLBACK_COUNTER_FILE"

#property triggering cache clustering
CACHE_MODE_PROPERTY="-Dcern.c2mon.cache.mode=multi"

COMMON_JAVA_ARGS="-Xms2048m -Xmx2048m -XX:+PrintGCDetails -XX:+UseParallelGC -XX:MaxGCPauseMillis=100 -Dserver.process.name=$PROCESS_NAME -Dtim.home=$C2MON_HOME -Dlog4j.configuration=$LOG4J_CONF_FILE -Dtim.log.dir=$LOG_DIR -Dtim.properties.location=$TIM_PROPERTIES -Dcom.sun.management.jmxremote.port=9523 -Dcom.sun.management.jmxremote.password.file=$HOME/.jmxremote.password -Dcom.sun.management.jmxremote.access.file=$HOME/.jmxremote.access -Dcom.sun.management.jmxremote.ssl=false"

CLUSTER_JAVA_ARGS="-Dcom.tc.l1.cachemanager.percentageToEvict=10 -Dcom.tc.l1.cachemanager.threshold=70 -Dcom.tc.l1.cachemanager.monitorOldGenOnly=false -Dtc.config=$TERRACOTTA_CONFIG $CACHE_MODE_PROPERTY"

#according to cache mode, set the JAVA args and the startup command (stop is common)
if [ ! "$2" == "single" ]; then
    C2MON_JAVA_ARGS="$COMMON_JAVA_ARGS $CLUSTER_JAVA_ARGS"  
    C2MON_START_CMD="$TERRACOTTA_HOME/bin/dso-java.sh $C2MON_JAVA_ARGS -classpath ${REQUIRED_LIBS} cern.tim.server.lifecycle.ServerStartup  $C2MON_ARGS"
else
    C2MON_JAVA_ARGS=$COMMON_JAVA_ARGS
    C2MON_START_CMD="$JAVA_HOME/jre/bin/java $C2MON_JAVA_ARGS -classpath $REQUIRED_LIBS  cern.tim.server.lifecycle.ServerStartup $C2MON_ARGS"
fi
    
C2MON_STOP_CMD="$JAVA_HOME/jre/bin/java -jar $APP_LIB_DIR/jmxterm-1.0-alpha-4-uber.jar -i $C2MON_HOME/bin/jmx-shutdown-script.txt -n -e -l localhost:$JMX_PORT  -u $JMX_USER -p $JMX_PASSWORD"

#export LD_LIBRARY_PATH=/opt/oc4j/jdbc/lib
#export PATH=$JAVA_HOME/jre/bin:$JAVA_HOME/bin:$LD_LIBRARY_PATH:$PATH

#if [ "$3" != "" ] ; then
#  echo "JAVA_HOME       : $JAVA_HOME"
  #echo "ORACLE_HOME     : $ORACLE_HOME"
#  echo "TERRACOTTA_HOME       : $TERRACOTTA_HOME"
#  echo
  #echo "PATH            : $PATH"
  #echo "LD_LIBRARY_PATH : $LD_LIBRARY_PATH"

#  echo "C2MON start cmd  : $C2MON_START_CMD"
#  echo "C2MON stop cmd   : $C2MON_STOP_CMD"
#fi

# Source function library.

if [ -f /etc/init.d/functions ] ; then
  . /etc/init.d/functions
elif [ -f /etc/rc.d/init.d/functions ] ; then
  . /etc/rc.d/init.d/functions
else
  exit 0
fi


RETVAL=0

# Call the runs function to find out if a process with
# a certain pid runs. The "runs" function will return
# 0 if the process is running, 1 if it is not.
# #Example: runs 23049
runs() {
  pid=${1##*/}
  tmp=`ps -p $pid -o pid=`
  if [ -z "$tmp" ] ; then
    return 1 
  else
    return 0 
  fi
}

# The start function checks whether the Oc4j service is already
# running and, if it think it is it, calls the really_start 
# function to launch it.
# Otherwise it exits with a warning message

start() {
	echo -n "Starting a C2MON server on $C2MON_HOST: "
        # Check if the PID file exists
        # If it exists, check whether the process is really running
        # If it is already running, print an error message and exit
        # If it is not running, clean up the PID and LOCK files and start OC4J 

        if [ -f $C2MON_PIDFILE ] ; then
            # Check if process with PID in PID files is running
            pid=`cat $C2MON_PIDFILE`
            runs $pid
            if [ $? -eq 1 ] ; then
              # It is not running --> remove PID file and LOCK file
              rm $C2MON_PIDFILE
#              if [ -f $OC4J_LOCKFILE ] ; then
#                rm $OC4J_LOCKFILE
#              fi
               
              really_start 
            else
              echo_warning
              echo           
              echo "A C2MON server seems to be running on $C2MON_HOST (pid= $pid)."
             fi
        else
	  #clean
          really_start
        fi
}	

# The really_start function tries to start the C2MON server
# and then checks if it running.

really_start() {
        cd $C2MON_HOME
        setsid $C2MON_START_CMD > $LOG_DIR/out.log 2> $LOG_DIR/err.log &
        
	pid=$! 
        sleep 1
        runs $pid
        if [ $? -eq 0 ] ; then
          echo $pid > $C2MON_PIDFILE
          #touch $C2MON_LOCKFILE
          echo_success
        else
          echo_failure
        fi
        echo
        return $chk
}

stop() {
	echo -n $"Shutting down the C2MON server on $C2MON_HOST ..."
        if [ -f $C2MON_PIDFILE ] ; then
          pid=`cat $C2MON_PIDFILE`

	  # First check if C2MON is running
	  runs $pid

	  if [ $? -eq 1 ] ; then
	    # C2MON is not running --> just remove the PID file
	    rm -f $C2MON_PIDFILE
	    echo_warning
	    echo
	    echo "C2MON server is not running on this host."
	  else
	    # C2MON is running --> try a gentle shutdown
	    #cd $C2MON_HOME
	    
	    #send JMX shutdown signal to server
	    $C2MON_STOP_CMD >> $LOG_DIR/out.log 2>> $LOG_DIR/err.log

	    #now gently kill the process
	    kill $pid >/dev/null 2>&1
	    PROC_RUNS=$?
	    PROC_WAIT=0;
	    while [ $PROC_RUNS -eq 0 ]; do
	      echo -n .
	      sleep 1 
	      if [ $PROC_WAIT -lt 30 ]; then
	        let PROC_WAIT=$PROC_WAIT+1
		runs $pid
		PROC_RUNS=$?
              else
	        PROC_RUNS=1
	      fi
	    done  
            runs $pid
	    if [ $? -eq 0 ] ; then
	      echo_warning
	      echo
	      echo -n "Gentle shutdown failed. Killing the C2MON server on $C2MON_HOST"
	      kill -9 $pid >/dev/null 2>&1
              sleep 1
	      runs $pid
              if [ $? -eq 1 ] ; then
                rm -f $C2MON_PIDFILE
                echo_success
		echo
                RETVAL=0
              else
                echo_failure
                echo
                echo "Unable to shut down C2MON server (supposed pid=$pid)."
                RETVAL=1
              fi
	    else
	      rm -f $C2MON_PIDFILE
	      echo_success
	      echo
              RETVAL=0
	    fi
	    
	  fi
        else
          echo_failure
          echo
          echo "No pid file ($C2MON_PIDFILE) found. If C2MON is running on ${C2MON_HOST}, kill it manually"
          RETVAL=1
        fi 
        return $RETVAL
}	

#restart() {
#	stop
#	start
#}	


status() {
        pid=
        if [ -f $C2MON_PIDFILE ]; then
                pid=`cat $C2MON_PIDFILE`
                runs $pid
                if [ $? -eq 0 ] ; then
                   echo "A C2MON server is running on $C2MON_HOST (pid=$pid)."
		   RETVAL=0
                else
                   echo "A pid file exists ($C2MON_PIDFILE) but the C2MON process is not running on $C2MON_HOST."
		   RETVAL=1
                fi
        else
          echo "A C2MON server does not seem to be running on $C2MON_HOST."
	  RETVAL=2
        fi
        return $RETVAL
}

silentcheck() {
  pid=
  if [ -f $C2MON_PIDFILE ]; then
    pid=`cat $C2MON_PIDFILE`
    runs $pid
    if [ $? -ne 0 ]; then
      cp -R $LOG_DIR $C2MON_HOME/`date +timlog_%Y-%m-%d_%H:%M:%S`
      rm $LOGDIR/*
      start
      echo "The status check script has restated C2MON on $HOSTNAME. Please check why the process was down (crash) and if it is starting up correctly. A backup of the log files was created in $C2MON_HOME/`date +timlog_%Y-%m-%d_%H:%M:%S`" | mail -s "Automatic C2MON restart on $HOSTNAME" $NOTIFY
    fi  
  fi
}

#C2MON_executeRemoteCmd() {
#  instruction=${1}

  # if not currently on the correct machine, run the command via ssh
  if [ `hostname -s` != $C2MON_HOST ] ; then    
    ssh -2 $C2MON_HOST $0 $1 $2
  # else run locally
  else

    case "$1" in
     'start')
         start
     ;;

     'stop')
         stop
     ;;

     'status')
  	 status
     ;;
	
     *)
	echo
	echo $"Usage: $0 {start|stop|restart|status} [second|single]"
	echo $"start [second|single] - Starts C2MON [second] server on the appropriate machine, if it is not running. If single is used, a single (non-clustered) server is started."
	echo $"status [second] - Checks the status (running/stopped) of the C2MON [second] server."
	echo $"stop [second] - Stops the C2MON [second] server on the appropriate host, if it is running. If a gentle shutdown fails, the process is killed after 30 seconds."
	exit 1
    esac
  fi
#}

#case "$1" in
#  start)
#  	start
#	;; 
#  stop)
#  	stop
#	;;
#  restart)
#  	restart
#	;;
#  status)
#  	status
#	;;
#  silentcheck)
#        silentcheck
#	;;
#  *)
#	echo $"Usage: $0 {start|stop|restart|status}"
#	echo $"start [second] - Starts C2MON server if it is not running"
#	echo $"stop  - Shuts down C2MON if it is running. If a gentle shutdown fails, the process is killed after 30 seconds."
#	exit 1
#esac

exit $?
