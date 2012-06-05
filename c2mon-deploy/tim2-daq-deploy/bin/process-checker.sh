#!/bin/sh
#
# Author: Matthias Braeger
#
# Description: 
# The intention of this script is to assure that there are not
# more DAQ processes running than existing PID files registered
# for the given DAQ host. This should assure that we do not run
# e.g. by mistake two DAQs for the same equipment.
#
# Please notice that this is a silent script, which means it will
# return something in case of a problem.
# 

DAQ_HOME=~/dist/daqprocess-mvn
SET_ENV_SCRIPT=$DAQ_HOME/bin/setenv.sh

#set the DAQ host machines if script is available
if [ -f $SET_ENV_SCRIPT ] ; then
  . $SET_ENV_SCRIPT
fi

if [[ $HOSTNAME =~ ${DAQ_OPC_HOST}.* ]] ; then
  DAQ_HOST=$DAQ_OPC_HOST
elif [[ $HOSTNAME =~ ${DAQ_PRIMARY_HOST}.* ]] ; then
  DAQ_HOST=$DAQ_PRIMARY_HOST
else
  echo "This is not a DAQ host!"
  exit 0
fi

PID_COUNTER=`ls $DAQ_HOME/tmp/$DAQ_HOST/*.pid | wc -l`
DAQ_PROCESS_COUNTER=`jps -v | grep P_ | wc -l`

if [ $PID_COUNTER != $DAQ_PROCESS_COUNTER ] ; then
 # email subject
 SUBJECT="Problem on DAQ host $DAQ_HOST"
 # Email To ?
 EMAIL="tim-admin@cern.ch"
 # Email text/message
 EMAILMESSAGE="Houston, we have a problem on DAQ host $DAQ_HOST! Counted $DAQ_PROCESS_COUNTER running DAQ processes but only $PID_COUNTER PID files."
 # send an email using /bin/mail
 echo $EMAILMESSAGE | /bin/mail -s "$SUBJECT" "$EMAIL"
fi

exit 0
