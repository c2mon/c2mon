# TIM DIPPublisher. CERN. All rights reserved.
#
# T Nick:           Date:       Info:
# -------------------------------------------------------------------------
# P mbrightw   6/Dec/2010       Re-adding support for multiple DIP publishers
# -------------------------------------------------------------------------

# Start/stop/restart TIM DIPPublisher

# Email address for notifications sent out by the silentcheck function
NOTIFY=tim.support@cern.ch,ti.operation@cern.ch

#####################
# DIP configuration #
#####################
export DIPNS=dipns1,dipns2
export DIM_DNS_NODE=dipns1,dipns2
export DIM_DNS_PORT=2506

########
# JAVA #
########
# Make sure the JAVA_BIN variable points to the java bin directory on Your machine
JAVA_BIN=/usr/java/jdk/jre/bin

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`

# Set DIP_PUBLISHER_HOME
DIP_PUBLISHER_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

#name of the DIP publisher passed as second argument
export PROCESS_NAME=$2

# The script which is actually calling the DIP publisher
STARTUP_SCRIPT=${DIP_PUBLISHER_HOME}/bin/DIP-PUBLISHER-STARTUP.jvm

# change into DIP_PUBLISHER_HOME
cd $DIP_PUBLISHER_HOME

PID_FILE="${DIP_PUBLISHER_HOME}/tmp/dippublisher_${PROCESS_NAME}.pid"

export PATH=${JAVA_BIN}:$PATH

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


DIPPublisher_start() {

  cd ${DIP_PUBLISHER_HOME}

  if [ -f $PID_FILE ] ; then
    echo "Could not start DIP Publisher ${PROCESS_NAME} !"
    echo "DipPublisher with that name is already running. Stop it first, please";
  else
   echo "Starting DIP Publisher ${PROCESS_NAME}.."
   ${STARTUP_SCRIPT} > ${DIP_PUBLISHER_HOME}/log/${PROCESS_NAME}.out.log 2>&1 &
   echo "$!" > ${PID_FILE}
  fi

}


DIPPublisher_stop() {

 cd ${DIP_PUBLISHER_HOME}

 if [ -f $PID_FILE ] ; then
   echo "Stopping DIP Publisher ${PROCESS_NAME}"
   pid=`cat $PID_FILE`
   kill -9 $pid >/dev/null 2>&1
   rm -f $PID_FILE
 fi

}

DIPPublisher_status() {

 cd ${DIP_PUBLISHER_HOME}

 echo "Checking status of DIP Publisher ${PROCESS_NAME}"

 if [ -f $PID_FILE ] ; then
   pid=`cat $PID_FILE`
   runs $pid
   if [ $? -eq 0 ] ; then
     echo "The dippublisher service $PROCESS_NAME is running (pid=$pid)."
     RETVAL=0
   else
     echo "A pid file exists ($PID_FILE) but the dippublisher process $PROCESS_NAME is not running."
     RETVAL=1
   fi
 else
   echo "The dippublisher service $PROCESS_NAME does not seem to be running."
   RETVAL=2
 fi
 return $RETVAL
}



DIPPublisher_restart() {

  DIPPublisher_stop
  sleep 1
  DIPPublisher_start

}

# This option is useful for a cronjob in order to
# check whether the dippublisher is still running.
# If this is not the case the dippublisher is restarted
DIPPublisher_silentcheck() {
  
  cd ${DIP_PUBLISHER_HOME}
  
  if [ -f $PID_FILE ]; then
    pid=`cat $PID_FILE`
    runs $pid
    if [ $? -ne 0 ]; then
      cp -Rfp ${DIP_PUBLISHER_HOME}/log ${DIP_PUBLISHER_HOME}/`date +log_%Y-%m-%d_%H:%M:%S`
      rm ${DIP_PUBLISHER_HOME}/log/${PROCESS_NAME}.*
      DIPPublisher_restart
      echo "The status check script has restarted the dippublisher process $PROCESS_NAME on $HOSTNAME. Please check why the process was down (crash) and if it is starting up correctly. A backup of the log files was created in ${DIP_PUBLISHER_HOME}/`date +log_%Y-%m-%d_%H:%M:%S`" | mail -s "Automatic dippublisher restart on $HOSTNAME" $NOTIFY
    fi
  fi
}



if [ -n "$2" ] ; then

  case "$1" in

   'start')
     DIPPublisher_start
     ;;
   'stop')
     DIPPublisher_stop
     ;;
   'restart')
     DIPPublisher_restart
     ;;
    'status')
     DIPPublisher_status
     ;;
   'silentcheck')
     DIPPublisher_silentcheck
     ;;
   *)
    echo "**************************************************************"
    echo " usage:"
    echo "   $0 start|stop|restart|status publisher_name"
    echo " e.g: $0 start DIPPub01"
    echo "**************************************************************"
   esac
  else
    echo "**************************************************************"
    echo " usage:"
    echo "   $0 start|stop|restart|status publisher_name"
    echo " e.g: $0 start DIPPub01"
    echo "**************************************************************"
  fi

exit $?
