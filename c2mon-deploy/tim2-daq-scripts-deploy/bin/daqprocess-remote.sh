#!/bin/sh

# TIM. CERN. All rights reserved.
#
# This scripts is used to start and stop the
# individual DAQ message handlers on the specified DAQ machines.
#
# Author: Matthias Braeger
# ------------------------------------------------------------------------------

# Start/stop/restart TIM2 DAQ process

#set home directory of script
SCRIPT=`readlink -f $(which $0)`
SCRIPTPATH=`dirname $SCRIPT`
DAQ_HOME=`dirname $SCRIPTPATH`
SET_ENV_SCRIPT=$DAQ_HOME/bin/setenv.sh

#set the DAQ host machines if script is available
if [ -f $SET_ENV_SCRIPT ] ; then
  . $SET_ENV_SCRIPT
fi

#stop if variable not set
if [ -z $DAQ_PRIMARY_HOST ]; then
 echo "DAQ_PRIMARY_HOST is not set"
 exit 1
fi

#stop if variable not set
if [ -z $DAQ_SECONDARY_HOST ]; then
 echo "DAQ_SECONDARY_HOST is not set"
 exit 1
fi

DAQ_HOST=`hostname -s`


# the name of the parameter determining that the DAQ start-up script
# should output only XML feedback messages

if [ "$1" == "-xml" ] ; then
  USE_XML_PROTOCOL=1
  export PROCESS_NAME=`echo $3 | tr 'a-z' 'A-Z'`
  PROCESS_COMMAND=$2
  export ADDITIONAL_PARAMS="$4 $5 $6 $7 $8 $9"
else
  USE_XML_PROTOCOL=0
  PROCESS_COMMAND=$1
  export PROCESS_NAME=`echo $2 | tr 'a-z' 'A-Z'`
  export ADDITIONAL_PARAMS="$3 $4 $5 $6 $7 $8 $9"
fi


#
# This function tries to find the DAQ process name in one of the module list files
# so that we can determine the execution host and the execution path.
#
TIMDAQ_setExecutionEnvironment() {
  LST_FILES=$DAQ_HOME/conf/*.lst
  for f in $LST_FILES
  do
    if [ `grep -c ${PROCESS_NAME} $f` -ge 1 ] ; then
      filename=`basename $f`
      DAQ_MODULE_NAME=${filename%.*}

      # Setting the execution host. All OPC DAQs are started on the primary host
      if [ $DAQ_MODULE_NAME = "opcua" ] ; then
        DAQ_HOST=$DAQ_PRIMARY_HOST
      else
        DAQ_HOST=$DAQ_SECONDARY_HOST
      fi
      break
    fi
  done

  if [ -z $DAQ_MODULE_NAME ]; then
    echo "DAQ ${PROCESS_NAME} is not known by the system."
    echo "Please update the configuration files in $LST_FILES"
    exit 1
  fi
}

#
# Example: TIMDAQ_executeCmd cs-ccr-tim1 start
#
TIMDAQ_executeCmd() {
  #creating command for starting DAQ from local module installation
  CMD="$PROCESS_COMMAND $PROCESS_NAME $ADDITIONAL_PARAMS"
  if [ $USE_XML_PROTOCOL -eq 1 ] ; then
    CMD="-xml $CMD"
  fi
  # The following line can be changed by the deployment.xml
  CMD="/opt/tim2-daq-${DAQ_MODULE_NAME}/bin/daqprocess.sh $CMD"

  # if we're currently not on the machine we the process should be started on, ssh to that machine
  if [ `hostname -s` != $DAQ_HOST ] ; then
    #override the trap on the shell EXIT to prevent "logout" output
    ssh -2 $DAQ_HOST "trap '' EXIT; $CMD"
  else
    $CMD
  fi
}

# Prints some instructions for the usage of this script.
# In particular it explains the supported arguments/options and how to use them.
TIMDAQ_printBasicUsageInfo() {
  if [ $USE_XML_PROTOCOL -eq 0 ] ; then
    echo "*****************************************************************************"
    echo " usage:                                                                      "
    echo " $0 [-xml] start|stop|restart|status process_name [additional options]               "
    echo
    echo " if -xml parameter is specified, only the XML output will be served          "
    echo
    echo " The additional options are :                                                "
    echo "  -s filename       {saves received conf.xml in a file}                      "
    echo "  -c filename       {starts the DAQ using predefined conf. file,instead of   "
    echo "                     asking the app.server}                                  "
    echo "  -eqLoggers        {if enabled, the DAQ will create seperate file appenders "
    echo "                     for all equipment message handlers loggers}             "
    echo "  -eqAppendersOnly  {if placed in pair with -eqLoggers, the emh's output     "
    echo "                     will be redirected to specific emh's appender files     "
    echo "                     only. EMH's output will not affect the process logger}  "
    echo "  -testMode         {starts the DAQ in test mode. no JMS connections will be "
    echo "                     established}                                            "
    echo "  -noDeadband       {disables all dynamic deadband filtering; static         "
    echo "                     deadbands remain active}                                "
    echo "  -transition       {starts the DAQ in transiton configuration (TIM1 as main "
    echo "                     , TIM2 as second}                                       "
    echo " e.g: $0 start P_TEST01 -testMode -c /tmp/testconf.xml                       "
    echo "*****************************************************************************"
  else
    TIMDAQ_EchoXMLFeedback -1 "Improper entry arguments for the TIM DAQ start-up script detected. Check the configuration, please"
  fi
}

# ##########################################################################################################
# ################################           Main Routine:             #####################################
# ##########################################################################################################

# turn on debug mode
#set -x

if [ -n "$PROCESS_NAME" ] ; then
  TIMDAQ_setExecutionEnvironment
  TIMDAQ_executeCmd
else
  TIMDAQ_printBasicUsageInfo
fi

