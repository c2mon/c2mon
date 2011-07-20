DAQ_HOME=~/dist/daqprocess2
DAQ_CONTROL=$DAQ_HOME/bin/daqprocess2.sh

#DAQ_HOSTS=(cs-ccr-tim4 cs-ccr-tim9)

pushd $DAQ_HOME/tmp/ >/dev/null 2>/dev/null
LS=`ls -1 *.pid`
popd >/dev/null 2>/dev/null

for pidfile in $LS; do
  proc=${pidfile:11}
  namelength=`expr index "$proc" .pid`-1
  proc=${proc:0:$namelength}
  $DAQ_CONTROL stop $proc
  sleep 1
done

pushd $DAQ_HOME/tmp/cs-ccr-tim1/ >/dev/null 2>/dev/null
LS=`ls -1 *.pid`
popd >/dev/null 2>/dev/null

for pidfile in $LS; do
  proc=${pidfile:11}
  namelength=`expr index "$proc" .pid`-1
  proc=${proc:0:$namelength}
  $DAQ_CONTROL stop $proc
  sleep 1
done
