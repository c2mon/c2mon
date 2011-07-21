#!/bin/sh
#
# sets the home directory
#
HOME=`dirname $0`
[[ $HOME == "." ]] && HOME=$PWD
HOME=$HOME/..

C2MON_DAQ_HOME=$HOME

C2MON_LOG=$C2MON_DAQ_HOME/log
C2MON_LIB=$C2MON_DAQ_HOME/lib

C2MON_DAQ_CONTROL=$C2MON_DAQ_HOME/bin/daqprocess2.sh


for proc in `cat $C2MON_DAQ_HOME/conf/daqprocess.lst`; do
  $C2MON_DAQ_CONTROL status $proc >/dev/null
  if [ $? -eq 0 ] ; then
    echo "$proc is running"
  else
    if [ -f $DAQ_HOME/conf/save_config/$proc.xml ] ; then
      echo Starting $proc with local configuration
      # $C2MON_DAQ_CONTROL restart $proc "-c $DAQ_HOME/conf/save_config/$proc.xml"
      $C2MON_DAQ_CONTROL stop $proc
      $C2MON_DAQ_CONTROL start $proc "-c $DAQ_HOME/conf/save_config/$proc.xml"
    else
      echo Starting $proc with online configuration.
      # $C2MON_DAQ_CONTROL restart $proc
      $C2MON_DAQ_CONTROL stop $proc
      $C2MON_DAQ_CONTROL start $proc
    fi
    sleep 1
  fi
done