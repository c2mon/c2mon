#!/bin/sh
#
# This script can be used to remove all DAQ pid files
# from processes which are not anymore running on the
# host from which the script is being executed
#
# Points to the DAQ2 directory.
#
# Author: Matthias Braeger
#

# sets the home directory
#
HOME=`dirname $0`
[[ $HOME == "." ]] && HOME=$PWD
HOME=$HOME/..

C2MON_DAQ_HOME=$HOME


# Temp directory of the DAQ processes
DAQ_TMP=${C2MON_DAQ_HOME}/tmp


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

################### Main routine ##########################

if [ -d ${DAQ_TMP}/${HOSTNAME} ] ; then
  for file in `ls ${DAQ_TMP}/${HOSTNAME}/*.pid` ; do
    pid=`cat $file | awk '{print $1}'` 
    runs $pid
    if [ $? -eq 1 ] ; then
      # It is not running --> remove PID file
      echo "Cleaned $file"
      rm $file
    fi
  done
else 
  echo "This machine is not a DAQ host!"
fi