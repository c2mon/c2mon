#!/bin/sh
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

#
# This script is used to start and stop the
# individual DAQ message handlers locally on the specified DAQ installations.
# To run correctly it has to be copied into the bin/ directory within the DAQ
# installation.
#
# ------------------------------------------------------------------------------


###############################
# Initialise helper functions #
###############################

BOOTUP=color
# column to start "[  OK  ]" label in 
RES_COL=60
# terminal sequence to move to that column. You could change this
# to something like "tput hpa ${RES_COL}" if your terminal supports it
MOVE_TO_COL="echo -en \\033[${RES_COL}G"
# terminal sequence to set color to a 'success' color (currently: green)
SETCOLOR_SUCCESS="echo -en \\033[0;32m"
# terminal sequence to set color to a 'failure' color (currently: red)
SETCOLOR_FAILURE="echo -en \\033[0;31m"
# terminal sequence to set color to a 'warning' color (currently: yellow)
SETCOLOR_WARNING="echo -en \\033[0;33m"
# terminal sequence to reset to the default color.
SETCOLOR_NORMAL="echo -en \\033[0;39m"

echo_success() {
  [ "$BOOTUP" = "color" ] && $MOVE_TO_COL
  echo -n "["
  [ "$BOOTUP" = "color" ] && $SETCOLOR_SUCCESS
  echo -n $"  OK  "
  [ "$BOOTUP" = "color" ] && $SETCOLOR_NORMAL
  echo -n "]"
  echo -ne "\r"
  return 0
}

echo_failure() {
  [ "$BOOTUP" = "color" ] && $MOVE_TO_COL
  echo -n "["
  [ "$BOOTUP" = "color" ] && $SETCOLOR_FAILURE
  echo -n $"FAILED"
  [ "$BOOTUP" = "color" ] && $SETCOLOR_NORMAL
  echo -n "]"
  echo -ne "\r"
  return 1
}

echo_warning() {
  [ "$BOOTUP" = "color" ] && $MOVE_TO_COL
  echo -n "["
  [ "$BOOTUP" = "color" ] && $SETCOLOR_WARNING
  echo -n $"WARNING"
  [ "$BOOTUP" = "color" ] && $SETCOLOR_NORMAL
  echo -n "]"
  echo -ne "\r"
  return 1
}

# Start/stop/restart C2MON DAQ process

#set home directory of script
SCRIPT=`readlink -f $(which $0)`
SCRIPTPATH=`dirname $SCRIPT`
export DAQ_HOME=`dirname $SCRIPTPATH`

DAQ_HOST=`hostname -s`

TIME=`date +"%F %T.%3N"`

# The DAQ script that actually starts the DAQ
DAQ_SCRIPT=${DAQ_HOME}/bin/C2MON-DAQ-STARTUP.jvm

# Variables declared as global variales (export) are also
# required by the C2MON-DAQ-STARTUP.jvm script
export DAQ_LOG_HOME=${DAQ_HOME}/log
export DAQ_CONF_HOME=$DAQ_HOME/conf
export C2MON_PROPERTIES_FILE=$DAQ_CONF_HOME/c2mon.properties
DAQ_LOG_FILE=${DAQ_LOG_HOME}/daqprocess.log

# the name of the parameter determining that the DAQ start-up script
# should output only XML feedback messages

if [ "$1" == "-xml" ] ; then
  USE_XML_PROTOCOL=1
  PROCESS_NAME=`echo $3 | tr 'a-z' 'A-Z'`
  PROCESS_COMMAND=$2
  ADDITIONAL_PARAMS="$4 $5 $6 $7 $8 $9"
else
  USE_XML_PROTOCOL=0
  PROCESS_COMMAND=$1
  PROCESS_NAME=`echo $2 | tr 'a-z' 'A-Z'`
  ADDITIONAL_PARAMS="$3 $4 $5 $6 $7 $8 $9"
fi

# Setting PID file name
PID_FILE="${DAQ_HOME}/tmp/${PROCESS_NAME}.pid"

# Check which log4j configuration script should be used
if [ -f ${DAQ_CONF_HOME}/${PROCESS_NAME}_log4j.xml ] ; then
  export  LOG4J_CONF_FILE=${DAQ_CONF_HOME}/${PROCESS_NAME}_log4j.xml
else
  # set the default one
  export LOG4J_CONF_FILE=${DAQ_CONF_HOME}/log4j.xml
fi

RETVAL=0

# runs()
# The function will check whether a process with the
# specified PID is currently running. It will return
# 0 if the process is running, 1 if it isn't.
#
# Example: runs 23049
#
runs() {
  pid=${1##*/}
  tmp=`ps -p $pid -o pid=`
  if [ -z "$tmp" ] ; then
    return 1
  else
    return 0
  fi
}


# this procedure prepars the XML execution feedback message.
# It takes the following arguments :
#   1. the execution code
#   2. the execution status
#   3. value (not mandatory)
DAQ_EchoXMLFeedback() {
  local EXEC_CODE=$1
  local EXEC_DESCR=$2
  local EXEC_VALUE=$3

  echo "<?xml version = \"1.0\"?>";
  echo "<execution-status>";
  echo "  <status-code>${EXEC_CODE}</status-code>";
  echo "  <status-description><![CDATA[${EXEC_DESCR}]]></status-description>";
  echo "</execution-status>";
  exit 0;
}

# This procedure starts the DAQ process only if it was not running yet.
DAQ_start() {
  cd ${DAQ_HOME}

  # Check if the DAQ process is already running
  # If it is, don't start it again.
  if [ -f $PID_FILE ] ; then
    pid=`cat $PID_FILE | awk {'print $1'}`
    host=`cat $PID_FILE | awk {'print $2'}`
    runs $pid
    if [ $? -eq 1 ] ; then
      rm $PID_FILE
      really_start
    else

      echo -e "$TIME\t$PROCESS_NAME\t$DAQ_HOST\tSTART\tFAILED\t(running)" >> $DAQ_LOG_FILE

      if [ $USE_XML_PROTOCOL -eq 0 ] ; then
         echo_warning
         echo "DAQ Process ${PROCESS_NAME} seems to be running on host $host. Stop it first."
      else
         DAQ_EchoXMLFeedback -1 "DAQ Process ${PROCESS_NAME} seems to be running. Stop it first."
      fi
    fi
  else
    really_start
  fi
}

# This producedure is in charge of:
# 1. Setting up some DAQ specific variables and deciding from where the DAQ process configuration should be read
# 2. Starting up the DAQ process by calling the common build autogenerated start-up script
# 3. Checking whether the process was started up successfully
really_start() {
  if [ -f ${DAQ_CONF_HOME}/local/${PROCESS_NAME}.xml ] ; then
    ADDITIONAL_PARAMS="${ADDITIONAL_PARAMS} -c ${DAQ_CONF_HOME}/local/${PROCESS_NAME}.xml"
  fi

  ADDITIONAL_PARAMS="${ADDITIONAL_PARAMS}"

  #Calls the script that was generated by the CommonBuild deployment procedure
  $DAQ_SCRIPT $ADDITIONAL_PARAMS $PROCESS_NAME >${DAQ_LOG_HOME}/${PROCESS_NAME}.out.log 2>&1 &
  
  if [ $USE_XML_PROTOCOL -eq 0 ] ; then
    echo -n "Starting DAQ Process ${PROCESS_NAME} on host ${DAQ_HOST} ..."
  fi

  pid="$!"
  sleep 5
  runs $pid

  if [ $? -eq 1 ] ; then

    echo -e "$TIME\t$PROCESS_NAME\t$DAQ_HOST\tSTART\tFAILED" >> $DAQ_LOG_FILE

    if [ $USE_XML_PROTOCOL -eq 0 ] ; then
      echo_failure
      echo "DAQ Process ${PROCESS_NAME} could not be started."
      echo
    else
      DAQ_EchoXMLFeedback -1 "DAQ Process ${PROCESS_NAME} could not be started."
    fi

  else

    echo "$pid $DAQ_HOST" > ${PID_FILE}
    echo -e "$TIME\t$PROCESS_NAME\t$DAQ_HOST\tSTART\tOK" >> $DAQ_LOG_FILE

    if [ $USE_XML_PROTOCOL -eq 0 ] ; then
      echo_success
      echo
    else
      DAQ_EchoXMLFeedback 0 OK
    fi

  fi
}


# This procedure tries to gently kill the DAQ process. In case that the process cannot be killed in that way,
# it will force it
DAQ_stop() {
 cd ${DAQ_HOME}

 if [ -f $PID_FILE ] ; then

   if [ $USE_XML_PROTOCOL -eq 0 ] ; then
     echo -n "Stopping DAQ Process ${PROCESS_NAME} on host ${DAQ_HOST} ..."
   fi

   pid=`cat $PID_FILE | awk {'print $1'}`
   kill $pid >/dev/null 2>&1
   runs $pid
   proc_runs=$?
   proc_wait=0
   while [ $proc_runs -eq 0 ] ; do
     if [ $USE_XML_PROTOCOL -eq 0 ] ; then
       echo -n .
     fi

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

    if [ $USE_XML_PROTOCOL -eq 0 ] ; then
      echo_warning
      echo
      echo -n "Unable to stop DAQ Process ${PROCESS_NAME} gently... killing it..."
     fi

     kill -9 $pid
     sleep 1
     runs $pid

     if [ $? -eq 1 ] ; then
       
       echo -e "$TIME\t$PROCESS_NAME\t$DAQ_HOST\tSTOP\tOK\t(kill -9)" >> $DAQ_LOG_FILE
       rm -f $PID_FILE

       if [ $USE_XML_PROTOCOL -eq 0 ] ; then
         echo_success
         echo
       elif [ $PROCESS_COMMAND == "stop" ] ; then # could also be called by restart command
         DAQ_EchoXMLFeedback 0 OK
       fi

       RETVAL=0
     else

       echo -e "$TIME\t$PROCESS_NAME\t$DAQ_HOST\tSTOP\tFAILED" >> $DAQ_LOG_FILE

       if [ $USE_XML_PROTOCOL -eq 0 ] ; then
         echo_failure
         echo
         echo "Unable to stop DAQ Process ${PROCESS_NAME}."
       elif [ $PROCESS_COMMAND == "stop" ] ; then # could also be called by restart command
         DAQ_EchoXMLFeedback -1 "Unable to stop DAQ Process ${PROCESS_NAME}."
       fi

       RETVAL=1
     fi

   else
   
     echo -e "$TIME\t$PROCESS_NAME\t$DAQ_HOST\tSTOP\tOK" >> $DAQ_LOG_FILE
     rm -f $PID_FILE

     if [ $USE_XML_PROTOCOL -eq 0 ] ; then
       echo_success
       echo
     elif [ $PROCESS_COMMAND == "stop" ] ; then # could also be called by restart command
       checkProcess=DAQ_status4XML
       if [ $? -eq 0 ] ; then
     	 DAQ_EchoXMLFeedback 0 OK
       else
         DAQ_EchoXMLFeedback -1 "DAQ Process ${PROCESS_NAME} does not seem to be running"
       fi
     fi

   fi

 else

   if [ $USE_XML_PROTOCOL -eq 0 ] ; then
     echo "DAQ Process ${PROCESS_NAME} does not seem to be running"
   elif [ $PROCESS_COMMAND == "stop" ] ; then # could also be called by restart command
     DAQ_EchoXMLFeedback -1 "DAQ Process ${PROCESS_NAME} does not seem to be running"
   fi

 fi

}


# Check whether the DAQ process is running
# The function will return 0 if the DAQ is
# found to be running, 1 if it isn't.
# It will also display some messages indicating
# the status of the DAQ process in the output
# stream
# ----------------------------------------
DAQ_status() {
  if [ -f $PID_FILE ]; then
    pid=`cat $PID_FILE`
    host=`cat $PID_FILE | awk {'print $2'}`
    runs $pid

    if [ $? -eq 0 ] ; then

      if [ $USE_XML_PROTOCOL -eq 0 ] ; then
   	    echo "RUNNING (host: $host  pid: $pid)"
      fi

      RETVAL=0
    else

      if [ $USE_XML_PROTOCOL -eq 0 ] ; then
        echo "DEAD (last time was running on host: $host) - cleaning PID file"
      fi

      rm -f $PID_FILE
      RETVAL=1
    fi
  else

    if [ $USE_XML_PROTOCOL -eq 0 ] ; then
      echo "STOPPED"
    fi

    RETVAL=1
  fi
  exit $RETVAL

}


  # Checks whether the DAQ Process is running.
  # The function will return 0 if the DAQ is found and
  # running and 1 if it isn't.
  DAQ_status4XML() {

   #returns
   # 0 - RUNNING
   # 2 - STOPPED

  if [ -f $PID_FILE ]; then
    pid=`cat $PID_FILE`
    runs $pid
    if [ $? -eq 0 ] ; then
      RETVAL=0
    else
      rm -f $PID_FILE
      RETVAL=1
    fi
  else
    RETVAL=1
  fi

  return $RETVAL
}



# Restart: stop the DAQ, the start it again
DAQ_restart() {
  DAQ_stop
  DAQ_start
}

# Prints some instructions for the usage of this script.
# In particular it explains the supported arguments/options and how to use them.
DAQ_printBasicUsageInfo() {
  if [ $USE_XML_PROTOCOL -eq 0 ] ; then
    echo "*****************************************************************************"
    echo " usage:                                                                      "
    echo " $0 [-xml] start|stop|restart|status process_name [additional options]       "
    echo
    echo " if -xml parameter is specified, only the XML output will be served          "
    echo
    echo " The additional options are :                                                "
    echo "  -s filename       {saves received conf.xml in a file}                      "
    echo "  -c filename       {starts the DAQ using predefined conf. file,instead of   "
    echo "                     asking the app.server}                                  "
    echo "  -t                {starts the DAQ in test mode. no JMS connections will be "
    echo "                     established}                                            "
    echo "  -d                {disables all dynamic time deadband filtering; static    "
    echo "                     deadbands remain active}                                "
    echo "                                                                             "
    echo " e.g: $0 start P_TEST01 -t -c /tmp/testconf.xml                       "
    echo "*****************************************************************************"
  else
    DAQ_EchoXMLFeedback -1 "Improper entry arguments for the C2MON DAQ start-up script detected. Check the configuration, please"
  fi
}

# ##########################################################################################################
# ################################           Main Routine:             #####################################
# ##########################################################################################################


  if [ -n "$PROCESS_NAME" ] ; then
    case "$PROCESS_COMMAND" in
     'start')
         DAQ_start
     ;;

     'stop')
         DAQ_stop
     ;;
     
     'restart')
         DAQ_restart
     ;;

     'status')
         DAQ_status
     ;;

     *)
       DAQ_printBasicUsageInfo
    esac
  else
    DAQ_printBasicUsageInfo
  fi

