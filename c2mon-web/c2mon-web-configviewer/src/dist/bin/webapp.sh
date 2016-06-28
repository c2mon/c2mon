#!/usr/bin/env bash

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



INSTALL_DIR="$(dirname "$(dirname "$(readlink "$0")")")"
LOG_DIR=$INSTALL_DIR/log
HOST_TMP_DIR=$INSTALL_DIR/tmp/
PIDFILE=$HOST_TMP_DIR/c2mon.pid

if [ "$1" == "run" ]  ; then
  export RUN_FOREGROUND="TRUE"
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

# The start function checks whether the service is already
# running and, if it think it is it, calls the really_start 
# function to launch it.
# Otherwise it exits with a warning message

start() {
  printf "Starting C2MON webapp: "
  # Check if the PID file exists
  # If it exists, check whether the process is really running
  # If it is already running, print an error message and exit
  # If it is not running, clean up the PID and LOCK files and start

  if [ -f $PIDFILE ] ; then
      # Check if process with PID in PID files is running
      pid=`cat $PIDFILE`
      runs $pid
      if [ $? -eq 1 ] ; then
        # It is not running --> remove PID file and LOCK file
        rm $PIDFILE

        really_start
      else
        echo_warning
        echo
        echo "The webapp seems to be already running"
       fi
  else
    really_start
  fi
}  

# The really_start function tries to start the service
# and then checks if it running.

really_start() {
  cd $INSTALL_DIR

  [[ -z "$JAVA_HOME" ]] && export JAVA_HOME=/usr/java/jdk
  CLASSPATH=`ls $INSTALL_DIR/lib/*.war | tr -s '\n' ':'`

  JVM_MEM=""
  JVM_OTHER_OPTS=()

  COMMAND=

  if [ -n "${RUN_FOREGROUND+set}" ] ; then
    echo "Running in foreground mode"

    exec -a `basename $0` $JAVA_HOME/bin/java -cp "$CLASSPATH" -Dc2mon.client.conf.url="$INSTALL_DIR/conf/c2mon-client.properties" \
      $JVM_MEM "${JVM_OTHER_OPTS[@]}" org.springframework.boot.loader.WarLauncher

  else
    exec -a `basename $0` $JAVA_HOME/bin/java -cp "$CLASSPATH" -Dc2mon.client.conf.url="$INSTALL_DIR/conf/c2mon-client.properties" \
     -Dlogging.file="log/c2mon-web-configviewer.log" $JVM_MEM "${JVM_OTHER_OPTS[@]}" org.springframework.boot.loader.WarLauncher > $LOG_DIR/out.log 2> $LOG_DIR/err.log &

    pid=$!
    sleep 1
    runs $pid
    if [ $? -eq 0 ] ; then
      echo $pid > $PIDFILE
      echo_success
    else
      echo_failure
    fi
    echo
    return $chk
  fi
}

stop() {
  printf $"Shutting down the webapp..."

  if [ -f $PIDFILE ] ; then
    pid=`cat $PIDFILE`

    # First check if C2MON is running
    runs $pid

    if [ $? -eq 1 ] ; then
      # tHE SERVICE is not running --> just remove the PID file
      rm -f $PIDFILE
      echo_warning
      echo
      echo "The webapp is not running on this host."
    else
      # The service is running --> try a gentle shutdown
      kill $pid >/dev/null 2>&1
      PROC_RUNS=$?
      PROC_WAIT=0;
      while [ $PROC_RUNS -eq 0 ]; do
        printf .
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
        echo "Gentle shutdown failed. Killing the webapp."
        kill -9 $pid >/dev/null 2>&1
        sleep 1
        runs $pid
        if [ $? -eq 1 ] ; then
          rm -f $PIDFILE
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
        rm -f $PIDFILE
        echo_success
        echo
        RETVAL=0
      fi
    fi
  else
    echo_failure
    echo
    echo "No pid file ($PIDFILE) found. If the webapp is running, kill it manually"
    RETVAL=1
  fi
  return $RETVAL
}  

status() {
  pid=
  if [ -f $PIDFILE ]; then
    pid=`cat $PIDFILE`
    runs $pid
    if [ $? -eq 0 ] ; then
       echo "The webapp is running (pid=$pid)."
       RETVAL=0
    else
       echo "A pid file exists ($PIDFILE) but the webapp is not running."
       RETVAL=1
    fi
  else
    echo "The webapp does not seem to be running."
    RETVAL=2
  fi
  return $RETVAL
}
# make tmp dir
if [ ! -d "$HOST_TMP_DIR" ]; then
  mkdir $HOST_TMP_DIR
fi

case "$1" in
'start')
  start
;;

'stop')
  stop
;;

'restart')
  stop && start
;;

'status')
  status
;;

'run')
  start
;;

*)
  echo
  echo $"Usage: $0 {start|stop|restart|status|run}"
  exit 1
esac

exit $?
