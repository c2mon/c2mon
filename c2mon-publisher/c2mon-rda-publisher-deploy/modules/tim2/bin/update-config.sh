#!/bin/bash

# The SMILE HTTP link to fetch the JAPC publication list 
JAPC_PUBLICATIONS_TID_URL="https://oraweb.cern.ch/pls/timw3_oper/smile.queryDisplay?nType=4&Tablename=VSML_360JAPCPUBPTS&vHeader=POINT_ID&vSelect=POINT_ID"

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

# The configuration file destination
CONF_FILE=$PUBLISHER_HOME/conf/publisher.tid
TEMP_FILE=$PUBLISHER_HOME/conf/publisher.new.xml 

# Get the new configuration
wget -O - -o /dev/null ${JAPC_PUBLICATIONS_TID_URL} | sort > ${TEMP_FILE}

# If the new configuration is different from the old one:
# (1) create a backup of the current configuration
# (2) replace the current configuration by the new one

if [ -s $CONF_FILE ]; then
  diff $CONF_FILE $TEMP_FILE >/dev/null
  if [ $? -eq 1 ] ; then
    echo "copying file..."
    cp $CONF_FILE $CONF_FILE.`date +%y%m%d_%k%M%S`
    mv $TEMP_FILE $CONF_FILE
    
    # The JAPC publisher is now restarted
    echo "Configuration has changed! Restarting JAPC publisher..."
    $PUBLISHER_HOME/bin/japc-sispublisher.sh restart $FILE_ROOT 
  else
    echo "The configuration has not changed since the last update!"
    rm $TEMP_FILE 	
  fi
else
  mv $TEMP_FILE $CONF_FILE
  echo "Configuration has changed! Restarting JAPC publisher..."
  $PUBLISHER_HOME/bin/japc-sispublisher.sh restart $FILE_ROOT
fi
