#!bin/bash
CLASSPATH=.:./libs
exec java -cp $CLASSPATH -Dtim.properties.location=~/.tim2.properties cern.tim.server.startup.ServerStartup