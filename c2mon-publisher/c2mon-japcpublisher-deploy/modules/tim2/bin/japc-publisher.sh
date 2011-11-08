#!/bin/sh

# TIM-JAPC-PUBLISHER for SIS system
#
# Author: Matthias Braeger (GS-ASE-SSE)
# Date:   Sept 2009
#
# This script is used to launch the JAPC-publisher on a linux
# server. 
# Please be aware that this script will NOT run from 
# everywhere but from the APP_HOME directory.  



##############################
# PROCESS specific variables #
##############################

PROCESS_NAME=japc-publisher
PROCESS_COMMAND=$1

#application home which shall contain a bin and conf directory
APP_HOME=$HOME/dist/japc-publisher

#The file that contains the Data Tag IDs which shall be published
TID_FILE=$APP_HOME/conf/publisher.tid

TIME=`date +"%F %T.%3N"`

########
# JAVA #
########

export JAVA_HOME=/usr/java/jdk
export PATH=$JAVA_HOME/bin:$JAVA_HOME/jre/bin:$PATH/
JAVA_BIN=$JAVA_HOME/jre/bin

###############
# DIRECTORIES #
###############

#log director
APP_LOG_HOME=/var/log/$USER/$PROCESS_NAME
if [ ! -d ${APP_LOG_HOME} ] ; then
    mkdir ${APP_LOG_HOME}
fi

#log file for this script
SCRIPT_LOG_DIR=${APP_HOME}/script-log
PROCESS_SCRIPT_LOG=$SCRIPT_LOG_DIR/$PROCESS_NAME.log
if [ ! -d ${SCRIPT_LOG_DIR} ] ; then
    mkdir ${SCRIPT_LOG_DIR}
fi

#graph configuration directory location
PROCESS_CONF_HOME=$APP_HOME/conf

#directory containing the libraries
REP_LIB_HOME=$APP_HOME/lib

#log4j configuration file
LOG4J_CONF_FILE=$PROCESS_CONF_HOME/log4j.xml


############################
# PID file and directories #
############################

PROCESS_HOST=`hostname -s`

#Create the pid directory structure for the local host, if necessary
if [ ! -d ${APP_HOME}/tmp/${PROCESS_HOST} ] ; then
  mkdir ${APP_HOME}/tmp/${PROCESS_HOST}
fi

PID_FILE=$APP_HOME/tmp/$PROCESS_HOST/$PROCESS_NAME.pid


##############
# LIBRAIRIES #
##############

# This command builds up a string that contains all jar file from $REP_LIB_HOME
REP_LIBS=`/bin/ls $REP_LIB_HOME | awk -v lib_home="$REP_LIB_HOME" 'BEGIN {OFS = ""; ORS = ":"} {print lib_home "/" $0}'`

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
# check if that process is already running on the local host
# before starting it
#-----------------------------------------------------------------
PROCESS_start() {
    cd ${APP_HOME}
    
    #if PID_FILE exists, check the process is not running anymore
    if [ -f $PID_FILE ] ; then
	pid=`cat $PID_FILE | awk {'print $1'}`
	runs $pid
	if [ $? -eq 1 ] ; then
	    rm $PID_FILE
	    really_start
	else
	 echo_warning
         echo "A $PROCESS_NAME Process seems to be running on host $PROCESS_HOST. Stop it first."
         echo -e "$TIME\t$PROCESS_NAME\t$PROCESS_HOST\tSTART\tFAILED\t(running)" >> $PROCESS_SCRIPT_LOG
	fi
    else
	really_start
    fi   
}


#--------------------------------------------------------------------
# start a process on the local machine on which the script is running
#--------------------------------------------------------------------
really_start() {

    $JAVA_BIN/java -Dtim.log.path=$APP_LOG_HOME -Dtim.process.name=$PROCESS_NAME -classpath ${REP_LIBS} ch.cern.tim.japc.publisher.JAPCPublisherKernel -dataTags $TID_FILE -log4j $LOG4J_CONF_FILE &

    echo -n "Starting a $PROCESS_NAME Process on host ${PROCESS_HOST} ..."

    pid="$!"
    sleep 5
    runs $pid

    if [ $? -eq 1 ] ; then
	echo_failure
	echo "A $PROCESS_NAME Process could not be started."
	echo
	echo -e "$TIME\t$PROCESS_NAME\t$PROCESS_HOST\tSTART\tFAILED" >> $PROCESS_SCRIPT_LOG
    
    else
        echo "$pid" > ${PID_FILE}
	echo_success
	echo
	echo -e "$TIME\t$PROCESS_NAME\t$PROCESS_HOST\tSTART\tOK" >> $PROCESS_SCRIPT_LOG
    fi
}

#--------------------------------------------------------
# stop the process, if it is running on the local machine
#--------------------------------------------------------
PROCESS_stop() {
    cd $APP_HOME

    #if PID file exists
    if [ -f $PID_FILE ] ; then
	echo -n "Stopping the $PROCESS_NAME Process on ${PROCESS_HOST} ..."
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
	    echo -n "Unable to stop the $PROCESS_NAME Process gently... killing it..."
	    kill -9 $pid
	    sleep 1
	    runs $pid

	    if [ $? -eq 1 ] ; then
		rm -f $PID_FILE 
		echo_success
		echo
		echo -e "$TIME\t$PROCESS_NAME\t$PROCESS_HOST\tSTOP\tOK\t(kill -9)" >> $PROCESS_SCRIPT_LOG
		RETVAL=0
	    else       
		echo_failure
		echo
		echo -e "$TIME\t$PROCESS_NAME\t$PROCESS_HOST\tSTOP\tFAILED" >> $PROCESS_SCRIPT_LOG
		echo "Unable to stop the $PROCESS_NAME Process."
		RETVAL=1
	    fi

	else
	    echo_success
	    echo
	    echo -e "$TIME\t$PROCESS_NAME\t$PROCESS_HOST\tSTOP\tOK" >> $PROCESS_SCRIPT_LOG
	    rm -f $PID_FILE
	fi
	
    #if PID file does not exist, is not running
    else
	echo "No $PROCESS_NAME Process seems to be running on ${PROCESS_HOST}"
    fi;
}


#----------------------------------------------------------------
# check the status of the $PROCESS_NAME process on the local machine:
# is an instance running or not?
#----------------------------------------------------------------
PROCESS_status() {
  if [ -f $PID_FILE ]; then
      pid=`cat $PID_FILE | awk {'print $1'}`
      runs $pid
      
      if [ $? -eq 0 ] ; then
	  echo "RUNNING (host: $PROCESS_HOST  pid: $pid)"
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


#----------------------------------------------------------------
# Restarts the $PROCESS_NAME process on the local machine
#----------------------------------------------------------------
PROCESS_restart() {
  PROCESS_stop
  sleep 1
  PROCESS_start
}


#----------------------------------------------------------------
# This option is useful for a cronjob in order to
# check whether the $PROCESS_NAME is still running.
# If this is not the case the $PROCESS_NAME is restarted
#----------------------------------------------------------------
PROCESS_silentcheck() {
  if [ -f $PID_FILE ]; then
    pid=`cat $PID_FILE`
    runs $pid
    if [ $? -ne 0 ]; then
      cp -Rfp ${APP_HOME}/log ${APP_HOME}/`date +log_%Y-%m-%d_%H:%M:%S`
      rm ${APP_HOME}/log/*
      PROCESS_restart
      echo "The status check script has restarted the process $PROCESS_NAME on $HOSTNAME. Please check why the process was down (crash) and if it is starting up correctly. A backup of the log files was created in ${APP_HOME}/`date +log_%Y-%m-%d_%H:%M:%S`" | mail -s "Automatic $PROCESS_NAME restart on $HOSTNAME" $NOTIFY
    fi
  fi
}

#----------------------------------------------------------------
# Prints the basic usage information
#----------------------------------------------------------------
PROCESS_printBasicUsageInfo() {
    echo "*****************************************************************"
    echo " usage:                                                          "
    echo " $PROCESS_NAME [start|stop|restart|status|silentcheck]           "
    echo "*****************************************************************"
}

##############################################################################
############################### MAIN ROUTINE #################################
##############################################################################

case "$PROCESS_COMMAND" in 
    
    'start')
	PROCESS_start
	;;

    'stop')
	PROCESS_stop
	;;

    'restart')
	PROCESS_restart
	;;
    'status')
	PROCESS_status
	;;
    'silentcheck')
	PROCESS_silentcheck
	;;

    *)
	PROCESS_printBasicUsageInfo
	;;
esac

