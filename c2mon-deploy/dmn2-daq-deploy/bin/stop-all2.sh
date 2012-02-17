#!/bin/sh
#
# sets the home directory
#

DAQ_HOME=`dirname $0`
[[ ${DAQ_HOME} == "." ]] && DAQ_HOME=$PWD
DAQ_HOME=${DAQ_HOME}/..


C2MON_LOG=$DAQ_HOME/log
C2MON_LIB=$DAQ_HOME/lib

JAVA_EXEC=/usr/java/jdk/bin/java

C2MON_DAQ_CONTROL=$DAQ_HOME/bin/daqprocess2.sh

pushd $DAQ_HOME/tmp/ >/dev/null 2>/dev/null
LS=`ls -1 *.pid`
popd >/dev/null 2>/dev/null

for pidfile in $LS; do
  proc=${pidfile:11}
  namelength=`expr index "$proc" .pid`-1
  proc=${proc:0:$namelength}
  $C2MON_DAQ_CONTROL stop $proc
  sleep 1
done