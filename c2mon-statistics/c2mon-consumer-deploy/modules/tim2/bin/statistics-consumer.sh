#!/bin/bash

# STATISTICS PACKAGE: CONSUMER PROCESS
#
# This script is part of the statistics module that calculates
# and displays statistics on TIM.
#
# The script manages the starting and stopping of the "consumer"
# process that reads the values being filtered out at the DAQ
# layer (via JMS), and writes these values to the database.
#
# The script allows one instance of the consumer process per machine.
# Use the "status" option to check if a instance is running on that local
# machine. The process id's of all consumer processes running can be found
# in the tmp folder in the statistics-consumer home directory.
#
# The JMS configuration details are specified in the consumer process
# configuration file; the database connection uses the ibatis package.

TIME=`date +"%F %T.%3N"`

###############
# DIRECTORIES #
###############

# get the current location
SCRIPT=`readlink -f $(which $0)`
SCRIPTPATH=`dirname $SCRIPT`
CONSUMER_HOME=$SCRIPTPATH/..

#log director
CONSUMER_LOG_HOME=$CONSUMER_HOME/log
if [ ! -d ${CONSUMER_LOG_HOME} ] ; then
    mkdir ${CONSUMER_LOG_HOME}
fi

#log file for this script
SCRIPT_LOG_DIR=${CONSUMER_HOME}/log
CONSUMER_SCRIPT_LOG=$SCRIPT_LOG_DIR/consumer-script.log
if [ ! -d ${SCRIPT_LOG_DIR} ] ; then
    mkdir ${SCRIPT_LOG_DIR}
fi

#graph configuration directory location
CONSUMER_CONF_HOME=$CONSUMER_HOME/conf

#log4j configuration file
LOG4J_CONF_FILE=$CONSUMER_CONF_HOME/log4j.xml


###########
# PROCESS #
###########

PROCESS_NAME=STATISTICS_CONSUMER
PROCESS_COMMAND=$1


############################
# PID file and directories #
############################

CONSUMER_HOST=`hostname -s`

#Create the pid directory structure for the local host, if necessary
if [ ! -d ${CONSUMER_HOME}/tmp/${CONSUMER_HOST} ] ; then
  mkdir ${CONSUMER_HOME}/tmp/${CONSUMER_HOST}
fi

PID_FILE=$CONSUMER_HOME/tmp/$CONSUMER_HOST/$PROCESS_NAME.pid

########################### 
# Source function library #
###########################

if [ -f /etc/init.d/functions ] ; then
  . /etc/init.d/functions
elif [ -f /etc/rc.d/init.d/functions ] ; then
  . /etc/rc.d/init.d/functions
else
  exit 0
fi


###################
# local functions #
###################

#---------------------------------------------------
# The function will check whether a process with the
# specified PID is currently running. It will return
# 0 if the process is running, 1 if it isn't.
#
# Example: runs 23049
#---------------------------------------------------
runs() {
  pid=${1##*/}
  tmp=`ps -p $pid -o pid=`
  if [ -z "$tmp" ] ; then
    return 1
  else
    return 0
  fi
}

#-----------------------------------------------------------------
# check if a consumer process is already running on the local host
# before starting the process
#-----------------------------------------------------------------
CONSUMER_start() {
    cd ${CONSUMER_HOME}
    
    #if PID_FILE exists, check the process is not running anymore
    if [ -f $PID_FILE ] ; then
	pid=`cat $PID_FILE | awk {'print $1'}`
	runs $pid
	if [ $? -eq 1 ] ; then
	    rm $PID_FILE
	    really_start
	else
	 echo_warning
         echo "A Consumer Process seems to be running on host $CONSUMER_HOST. Stop it first."
         echo -e "$TIME\t$PROCESS_NAME\t$CONSUMER_HOST\tSTART\tFAILED\t(running)" >> $CONSUMER_SCRIPT_LOG
	fi
    else
	really_start
    fi   
}


#--------------------------------------------------------------------------
# start a consumer process on the local machine on which the script is run
#--------------------------------------------------------------------------
really_start() {
    
    #export variables for the CommonBuild script
    JAVA_HOME=/usr/java/jdk
    export PATH=$JAVA_HOME/bin:$JAVA_HOME/jre/bin:$PATH
    export PATH=${CONSUMER_HOME}/bin:$PATH
    export CONSUMER_LOG_HOME
    export CONSUMER_HOME
    export LOG4J_CONF_FILE    
    
    #need to be in bin directory for startup script
    cd $CONSUMER_HOME/bin

    setsid C2MON-STATISTICS-CONSUMER.jvm >${CONSUMER_LOG_HOME}/${PROCESS_NAME}.out.log 2>&1 &

    echo -n "Starting a Consumer Process on host ${CONSUMER_HOST} ..."

    pid="$!"
    sleep 5
    runs $pid

    if [ $? -eq 1 ] ; then
	echo_failure
	echo "A Consumer Process could not be started."
	echo
	echo -e "$TIME\t$PROCESS_NAME\t$CONSUMER_HOST\tSTART\tFAILED" >> $CONSUMER_SCRIPT_LOG
    
    else
        echo "$pid" > ${PID_FILE}
	echo_success
	echo
	echo -e "$TIME\t$PROCESS_NAME\t$CONSUMER_HOST\tSTART\tOK" >> $CONSUMER_SCRIPT_LOG
    fi
}

#--------------------------------------------------------
# stop the process, if it is running on the local machine
#--------------------------------------------------------
CONSUMER_stop() {
    cd $CONSUMER_HOME

    #if PID file exists
    if [ -f $PID_FILE ] ; then
	echo -n "Stopping the Consumer Process on ${CONSUMER_HOST} ..."
	pid=`cat $PID_FILE | awk {'print $1'}`
	kill $pid >/dev/null 2>&1
	runs $pid
	proc_runs=$?
	proc_wait=0
	while [ $proc_runs -eq 0 ] ; do
	    echo -n .
	    sleep 1
	    if [ $proc_wait -lt 10 ] ; then
		let proc_wait=$proc_wait+1
		runs $pid
		proc_runs=$?
	    else
		proc_runs=1
	    fi
	done
	runs $pid

	if [ $? -eq 0 ] ; then  
	    echo_warning
	    echo
	    echo -n "Unable to stop the Consumer Process gently... killing it..."
	    kill -9 $pid
	    sleep 1
	    runs $pid

	    if [ $? -eq 1 ] ; then
		rm -f $PID_FILE 
		echo_success
		echo
		echo -e "$TIME\t$PROCESS_NAME\t$CONSUMER_HOST\tSTOP\tOK\t(kill -9)" >> $CONSUMER_SCRIPT_LOG
		RETVAL=0
	    else       
		echo_failure
		echo
		echo -e "$TIME\t$PROCESS_NAME\t$CONSUMER_HOST\tSTOP\tFAILED" >> $CONSUMER_SCRIPT_LOG
		echo "Unable to stop the Consumer Process."
		RETVAL=1
	    fi

	else
	    echo_success
	    echo
	    echo -e "$TIME\t$PROCESS_NAME\t$CONSUMER_HOST\tSTOP\tOK" >> $CONSUMER_SCRIPT_LOG
	    rm -f $PID_FILE
	fi
	
    #if PID file does not exist, is not running
    else
	echo "No Consumer Process seems to be running on ${CONSUMER_HOST}"
    fi;
}

#----------------------------------------------------------------
# check the status of the Consumer process on the local machine:
# is an instance running or not?
#----------------------------------------------------------------
CONSUMER_status() {
  if [ -f $PID_FILE ]; then
      pid=`cat $PID_FILE | awk {'print $1'}`
      runs $pid
      
      if [ $? -eq 0 ] ; then
	  echo "RUNNING (host: $CONSUMER_HOST  pid: $pid)"
	  RETVAL=0
      else
	  echo "DEAD - cleaning PID file"
	  rm -f $PID_FILE
	  RETVAL=1
      fi
  else
      echo "STOPPED"
      RETVAL=1
  fi
  exit $RETVAL
}




CONSUMER_printBasicUsageInfo() {
    echo "*****************************************************************"
    echo " usage:                                                          "
    echo " statistics-consumer.sh [start|stop|status]                      "
    echo "*****************************************************************"
}

##############################################################################
############################### MAIN ROUTINE #################################
##############################################################################

case "$PROCESS_COMMAND" in 
    
    'start')
	CONSUMER_start
	;;

    'stop')
	CONSUMER_stop
	;;

    'status')
	CONSUMER_status
	;;

    *)
	CONSUMER_printBasicUsageInfo
	;;
esac


