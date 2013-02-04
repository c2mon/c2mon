#!/bin/sh

# TIM. CERN. All rights reserved.
#
# This scripts is used to start/stop/restart all the
# individual DAQ message handlers on the specified DAQ machines.
#
# Author: Matthias Braeger
# ------------------------------------------------------------------------------

#set home directory of script
SCRIPT=`readlink -f $(which $0)`
SCRIPTPATH=`dirname $SCRIPT`
DAQ_HOME=`dirname $SCRIPTPATH`


##
# Starts/Stops/Restarts all known DAQs by scanning the *.lst files in the
# ../conf folder
#
exec_all() {
  daq_cmb=$1
  echo "Restarting all known DAQ modules..."
  echo
  LST_FILES=$DAQ_HOME/conf/*.lst
  for f in $LST_FILES
  do
    exec_all_in_list $daq_cmb $f 
  done
}

##
# Starts/Stops/Restarts all DAQs which are listed in the provided file. Only one
# DAQ name is allowed per line.
#
exec_all_in_list() {
  daq_cmb=$1
  f=$2
  for daqname in $(cat $f)
  do 
    daqprocess $daq_cmb $daqname
  done
}

##
# Prints the User Help
#
print_usage() {
  echo "*****************************************************************************"
  echo " Usage:                                                                      "
  echo " $0 start|stop|restart [additional option]                                   "
  echo
  echo " Additional options are :                                                    "
  echo "  -a --all          Stops/Starts/Restarts all known DAQs                     "
  echo "  -f filename       Stops/Starts/Restarts all DAQs which names are contained "
  echo "                    in the given DAQ name list file                          "
  echo "  -h --help         Prints this help message                                 "
  echo
  echo " e.g: $0 stop -f /tmp/opcua.lst                                              "
  echo "*****************************************************************************"
}

# ##########################################################################################################
# ################################           Main Routine:             #####################################
# ##########################################################################################################

# turn on debug mode
#set -x

if [ "$#" == "0" ] ; then
  print_usage
elif [ "$1" == "start" ] || [ "$1" == "stop" ] || [ "$1" == "restart" ] ; then
  daq_cmd="$1"

  if [ "$2" == "" ] ; then
    print_usage
  elif [ "$2" == "-a" ] || [ "$2" == "--all" ] ; then
    exec_all $daq_cmd
  elif [ "$2" == "-f" ] ; then
    daqname_lst=$3
    if [ "$daqname_lst" == "" ] ; then
      print_usage
    elif [ -f $daqname_lst ] ; then
      exec_all_in_list $daq_cmd $daqname_lst
    else
      echo "File $daqname_lst does not exist"
      exit 1
    fi
  else
    print_usage
  fi
else
  print_usage
fi
