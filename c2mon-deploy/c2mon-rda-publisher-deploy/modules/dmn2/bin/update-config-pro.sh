#!/bin/bash


# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`

# Set PUBLISHER_HOME
PUBLISHER_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

#the script to be called to get the rda-published tag id from db 
JAPC_PUB_TID_GENERATOR=${PUBLISHER_HOME}/bin/tid-generator-pro.pl

# The configuration file destination
CONF_FILE=$PUBLISHER_HOME/conf/publisher.tid
TEMP_FILE=$PUBLISHER_HOME/conf/publisher-new.tid 

# Get the new configuration
`${JAPC_PUB_TID_GENERATOR}`


# If the new configuration is different from the old one:
# (1) create a backup of the current configuration
# (2) replace the current configuration by the new one

if [ -s $CONF_FILE ]; then
  diff $CONF_FILE $TEMP_FILE >/dev/null
  if [ $? -eq 1 ] ; then
    echo "copying file..."
    cp $CONF_FILE $CONF_FILE.`date +%y%m%d_%k%M%S`
    mv $TEMP_FILE $CONF_FILE
    
    echo "Configuration has changed! The Publisher will automatically subscribe to any new tag IDs within the next 60 seconds."
  else
    echo "The configuration has not changed since the last update!"
    rm -rf $TEMP_FILE 2>&1 > /dev/null  
  fi
else
  mv $TEMP_FILE $CONF_FILE
  echo "Configuration has changed! The Publisher will automatically subscribe to any new tag IDs within the next 60 seconds."
fi