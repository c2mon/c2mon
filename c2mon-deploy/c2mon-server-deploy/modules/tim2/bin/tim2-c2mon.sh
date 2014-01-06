#!/bin/bash
#
# For starting and stopping a C2MON server on the local machine.

########################
# DEPLOYMENT VARIABLES #
########################

#set this mode to SINGLE to start a single non-clustered server
MODE=distributed
#MODE=single

#export Java for TC script also!

# get the current location 
SCRIPT=`readlink -f $(which $0)`
SCRIPTPATH=`dirname $SCRIPT`
C2MON_HOME=$SCRIPTPATH/..

#set env. variables if script is available
if [ -f $C2MON_HOME/bin/setenv.sh ] ; then
  . $C2MON_HOME/bin/setenv.sh
fi

#.c2mon.properties location
C2MON_PROPERTIES=$C2MON_HOME/conf/c2mon.properties

C2MON_JMX_REMOTE_ACCESS=$C2MON_HOME/conf/jmxremote.access
C2MON_JMX_REMOTE_PASSWD=$C2MON_HOME/conf/jmxremote.passwd

#################
# COMMON SETUP  #
#################

#log4j configuration file
LOG4J_CONF_FILE=$C2MON_HOME/conf/log4j.xml
#log directory
LOG_DIR=$C2MON_HOME/log
#directory of shared libraries
SHARED_LIB_HOME=$HOME/dist/libs

###################
# CLUSTERED SETUP #
###################

#Terracotta configuration location (either file or host:port)
TC_CONFIG_PATH=$TERRACOTTA_HOST:$TERRACOTTA_TSA_PORT

#add Terracotta mirror if set
if [ ! -z $TERRACOTTA_MIRROR_HOST ] && [ ! -z $TERRACOTTA_MIRROR_TSA_PORT ]; then
    TC_CONFIG_PATH=$TC_CONFIG_PATH,$TERRACOTTA_MIRROR_HOST:$TERRACOTTA_MIRROR_TSA_PORT
fi

####################
# MORE SETTINGS... #
####################

#set correct host
HOST_TMP_DIR=$C2MON_HOME/tmp/

#set correct PID file
C2MON_PIDFILE=$HOST_TMP_DIR/c2mon.pid

##############
# LIBRAIRIES #
##############

#for jmx lifecycle
JMXJAR=$SHARED_LIB_HOME/jmxterm/jmxterm-1.0-alpha-4-uber.jar

#######################
# start/stop commands #
#######################

CLASSPATH=`ls $C2MON_HOME/lib/*.jar | tr -s '\n' ':'`

#-Dtim.log.fallback.file=$TIM_LOG_FALLBACK_FILE -Dtim.log.fallback.counter.file=$TIM_LOG_FALLBACK_COUNTER_FILE"

#property triggering cache clustering
CACHE_MODE_PROPERTY="-Dcern.c2mon.cache.mode=multi"

COMMON_JAVA_ARGS="-Xms2048m -Xmx2048m -XX:NewRatio=3 -XX:+PrintGCDetails -XX:+UseParallelGC -XX:MaxGCPauseMillis=100 -Dserver.process.name=$PROCESS_NAME -Dc2mon.process.name=$PROCESS_NAME -Dc2mon.home=$C2MON_HOME -Dlog4j.configuration=$LOG4J_CONF_FILE -Dc2mon.log.dir=$LOG_DIR -Dc2mon.properties.location=$C2MON_PROPERTIES -Dcom.sun.management.jmxremote.port=$JMX_PORT -Dcom.sun.management.jmxremote.password.file=$C2MON_JMX_REMOTE_PASSWD -Dcom.sun.management.jmxremote.access.file=$C2MON_JMX_REMOTE_ACCESS -Dcom.sun.management.jmxremote.ssl=false -Dlaser.hosts=$LASER_HOSTS -Dcmw.mom.brokerlist=$CMW_BROKER_LIST"

CLUSTER_JAVA_ARGS="-Dcom.tc.l1.cachemanager.percentageToEvict=10 -Dcom.tc.l1.cachemanager.threshold=70 -Dcom.tc.l1.cachemanager.monitorOldGenOnly=false -Dterracotta.config.location=$TC_CONFIG_PATH $CACHE_MODE_PROPERTY -Dcom.tc.productkey.path=$C2MON_HOME/conf/terracotta-license.key"

if [ "$1" == "recover" ]; then
    C2MON_RECOVERY_ARG="-Dc2mon.recovery=true"
fi

#according to cache mode, set the JAVA args and the startup command (stop is common)

if [ ! MODE == "single" ]; then
    C2MON_JAVA_ARGS="$COMMON_JAVA_ARGS $CLUSTER_JAVA_ARGS $C2MON_RECOVERY_ARG"  
    C2MON_STOP_CMD="$JAVA_HOME/jre/bin/java -jar $JMXJAR -i $C2MON_HOME/bin/jmx-shutdown-script.txt -n -e -l localhost:$JMX_PORT  -u $JMX_USER -p $JMX_PASSWORD"
else
    C2MON_JAVA_ARGS="$COMMON_JAVA_ARGS $C2MON_RECOVERY_ARG"
    C2MON_STOP_CMD="echo \"attempting to shutdown the server with kill call\""
fi
C2MON_START_CMD="$JAVA_HOME/jre/bin/java $C2MON_JAVA_ARGS -cp "${CLASSPATH}" cern.tim.server.lifecycle.ServerStartup $C2MON_ARGS"

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
	echo -n "Starting a C2MON server: "
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
               
              really_start 
            else
              echo_warning
              echo           
              echo "This C2MON server seems to be already running"
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
	echo -n $"Shutting down this C2MON server..."
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
	      if [ $PROC_WAIT -lt 20 ]; then
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
	      echo "Gentle shutdown failed. Killing this C2MON server."
	      echo "To ensure all data was processed, one of the following actions should be taken: "
	      echo "  - either restart a server using the *recover* option"
	      echo "  - or use the JConsole to run the *RecoveryManager -> recover task* (on other running server for instance)"
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
          echo "No pid file ($C2MON_PIDFILE) found. If this C2MON server is running, kill it manually"
          RETVAL=1
        fi 
        return $RETVAL
}	

status() {
        pid=
        if [ -f $C2MON_PIDFILE ]; then
                pid=`cat $C2MON_PIDFILE`
                runs $pid
                if [ $? -eq 0 ] ; then
                   echo "This C2MON server is running (pid=$pid)."
		   RETVAL=0
                else
                   echo "A pid file exists ($C2MON_PIDFILE) but the C2MON process is not running."
		   RETVAL=1
                fi
        else
          echo "This C2MON server does not seem to be running."
	  RETVAL=2
        fi
        return $RETVAL
}
    #make tmp dir on correct machine  
    if [ ! -d "$HOST_TMP_DIR" ]; then
	mkdir $HOST_TMP_DIR
    fi

    case "$1" in
     'start')
         start
     ;;

     'recover')
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
	echo $"Usage: $0 {start|stop|recover|status}"
	echo $"start - Starts C2MON server on this machine, if it is not running."
	echo $"recover - Same as start command, but with extra functionality for recovering after a server crash."
	echo $"status - Checks the status (running/stopped) of the C2MON server."
	echo $"stop - Stops the C2MON server on this host, if it is running. If a gentle shutdown fails, the process is killed after 30 seconds."
	exit 1
    esac


exit $?
