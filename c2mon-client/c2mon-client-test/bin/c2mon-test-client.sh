#!/bin/bash

#starts n c2mon clients connecting to TAG_NUMBER tags, where n is passed as argument
#e.g. c2mon-test-client.sh 5 will start 5 clients

export TAG_NUMBER=1000

if [ ! -n $1 ]
then
    MAX=1
else
    MAX=$1
fi

for ((i=1; i<=$MAX; i++))
 do
  export PROCESS_NAME=c2mon-test-client-$i
  ./C2MON-TEST-CLIENT.jvm > ../log/out.log 2>&1 &
  sleep 10s
 done
