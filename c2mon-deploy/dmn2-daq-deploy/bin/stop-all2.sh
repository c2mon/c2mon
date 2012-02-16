#!/bin/sh
#
# sets the home directory
#

# sets the home directory
#
DAQ_HOME=`dirname $0`
[[ ${DAQ_HOME} == "." ]] && DAQ_HOME=$PWD
DAQ_HOME=${DAQ_HOME}/..

C2MON_LOG=$DAQ_HOME/log
C2MON_LIB=$DAQ_HOME/lib

C2MON_DAQ_CONTROL=$DAQ_HOME/bin/daqprocess2.sh


for proc in `cat $DAQ_HOME/conf/daqprocess.lst`; do
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
dmndev@cs-ccr-lasertest:/opt/dmn2-daq-dev/bin)cp ../conf/daqprocess.lst ~/c2mon/daq/
dmndev@cs-ccr-lasertest:/opt/dmn2-daq-dev/bin)cat stop-all2.sh
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