#!/bin/bash

#Script for stopping and starting the Terracotta cluster of servers.

#Provides options for the initial startup of a TC server and for stopping/restarting one of the nodes.

#In usual C2MON operation, neither TC server should need starting/stopping: only if one of them fails should it be restarted.

#If both nodes of the cluster fail, the "init" option must be used to start the initial node of the cluster (if this
# fails, the file DB may need deleting under $TC_LOG_DIR/server-data, possibly on both node machines)

TC_CONFIG_DIR=~/dist/terracotta/conf
TC_CONFIG_FILE=$TC_CONFIG_DIR/c2mon-tc-config.xml
TC_HOME=/opt/terracotta
TC_LOG_DIR=~/dist/terracotta/log
TC_MAIN_HOST=cs-ccr-tim11
TC_MAIN_NAME=server1
TC_MAIN_DSO_PORT=9510
TC_MAIN_JMX_PORT=9520

TC_MIRROR_HOST=cs-ccr-tim12
TC_MIRROR_NAME=server2
TC_MIRROR_DSO_PORT=9511
TC_MIRROR_JMX_PORT=9521

if [ "$2" == "standby" ]; then
    TC_HOST=$TC_MIRROR_HOST
    TC_NAME=$TC_MIRROR_NAME
    TC_DSO_PORT=$TC_MIRROR_DSO_PORT
    TC_JMX_PORT=$TC_MIRROR_JMX_PORT
    TC_OTHER_HOST=$TC_MAIN_HOST
    TC_OTHER_DSO_PORT=$TC_MAIN_DSO_PORT
else
    TC_HOST=$TC_MAIN_HOST
    TC_NAME=$TC_MAIN_NAME
    TC_DSO_PORT=$TC_MAIN_DSO_PORT
    TC_JMX_PORT=$TC_MAIN_JMX_PORT
    TC_OTHER_HOST=$TC_MIRROR_HOST
    TC_OTHER_DSO_PORT=$TC_MIRROR_DSO_PORT
fi

#if initial start (both TC servers stopped), get config from file (ssh if necessary)
if [ "$1" == "start" ]; then
    echo "Initialising the cluster by starting a Terracotta server on $TC_HOST"
    ssh $TC_HOST "source ~/.profile; setsid $TC_HOME/bin/start-tc-server.sh -n $TC_NAME -f $TC_CONFIG_FILE > $TC_LOG_DIR/out.log 2> $TC_LOG_DIR/err.log &" > /dev/null 2>&1
#if start with other TC server running, get config from other server (ssh if necessary)
elif [ "$1" == "join" ]; then
    echo "Starting a Terracotta server on $TC_HOST to join the existing cluster (running server is on host $TC_OTHER_HOST)"
    ssh $TC_HOST "source ~/.profile; setsid $TC_HOME/bin/start-tc-server.sh -n $TC_NAME -f $TC_OTHER_HOST:$TC_OTHER_DSO_PORT > $TC_LOG_DIR/out.log 2> $TC_LOG_DIR/err.log &" > /dev/null 2>&1
#ssh to make sure TC_HOME exists
elif [ "$1" == "stop" ]; then
    echo "Stopping the Terracotta server on host $TC_HOST"
    ssh $TC_HOST "source ~/.profile; $TC_HOME/bin/stop-tc-server.sh $TC_HOST $TC_JMX_PORT" > /dev/null 2>&1
else

    echo "Usage: terracotta {start,join,stop} [standby]"
    echo " start [standby] - start the initial TC server in a cluster ([standby] for starting the usual mirror as the main server)"
    echo " join [standby] - start a TC server to join an existing cluster ([standby] for starting the second TC server)"
    echo " stop [standby] - for stopping the [standby] server"

fi
exit 0