# sets the home directory
#
HOME=`dirname $0`
[[ $HOME == "." ]] && HOME=$PWD
HOME=$HOME/..

C2MON_DAQ_HOME=$HOME

C2MON_LOG=$C2MON_DAQ_HOME/log
C2MON_LIB=$C2MON_DAQ_HOME/lib

JAVA_EXEC=/usr/java/jdk/bin/java

C2MON_DAQ_CONTROL=$C2MON_DAQ_HOME/bin/daqprocess2.sh

#DAQ_HOSTS=(cs-ccr-tim4 cs-ccr-tim9)

pushd $C2MON_DAQ_HOME/tmp/ >/dev/null 2>/dev/null
LS=`ls -1 *.pid`
popd >/dev/null 2>/dev/null

for pidfile in $LS; do
  proc=${pidfile:11}
  namelength=`expr index "$proc" .pid`-1
  proc=${proc:0:$namelength}
  $C2MON_DAQ_CONTROL stop $proc
  sleep 1
  done

pushd $C2MON_DAQ_HOME/tmp/cs-ccr-tim1/ >/dev/null 2>/dev/null
LS=`ls -1 *.pid`
popd >/dev/null 2>/dev/null

for pidfile in $LS; do
  proc=${pidfile:11}
  namelength=`expr index "$proc" .pid`-1
  proc=${proc:0:$namelength}
  $C2MON_DAQ_CONTROL stop $proc
  sleep 1
done

pushd $C2MON_DAQ_HOME/tmp/cs-ccr-tim6/ >/dev/null 2>/dev/null
LS=`ls -1 *.pid`
popd >/dev/null 2>/dev/null

for pidfile in $LS; do
  proc=${pidfile:11}
  namelength=`expr index "$proc" .pid`-1
  proc=${proc:0:$namelength}  
  $C2MON_DAQ_CONTROL stop $proc
  sleep 1 
done


pushd $C2MON_DAQ_HOME/tmp/cs-ccr-tim9/ >/dev/null 2>/dev/null
LS=`ls -1 *.pid`
popd >/dev/null 2>/dev/null

for pidfile in $LS; do
  proc=${pidfile:11}
  namelength=`expr index "$proc" .pid`-1
  proc=${proc:0:$namelength}
  $C2MON_DAQ_CONTROL stop $proc
  sleep 1
done
