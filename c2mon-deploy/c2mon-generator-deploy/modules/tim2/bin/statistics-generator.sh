#!/bin/bash

#STATISTICS PACKAGE: CHART WEB DEPLOYER
#
#This script is part of the statistics module that calculates
#and displays statistics on TIM.
#
#This script generates the charts and html fragments
#for the statistics module, and saves them in the appropriate
#directories.
#
#It must be run on the machine that is hosting the chart and html
#directories.
#
#To run on dev, switch HOME_TEST from dist to dev
#

########
# JAVA #
########

export JAVA_HOME=/usr/java/jdk
export PATH=$JAVA_HOME/bin:$JAVA_HOME/jre/bin:$PATH/
JAVA_BIN=$JAVA_HOME/jre/bin



###############
# DIRECTORIES #
###############

# get the current location
SCRIPT=`readlink -f $(which $0)`
SCRIPTPATH=`dirname $SCRIPT`
STATISTICS_GENERATOR_HOME=$SCRIPTPATH/..

#log director
STATISTICS_GENERATOR_LOG_HOME=$STATISTICS_GENERATOR_HOME/log

#configuration directory location
STATISTICS_GENERATOR_CONF_HOME=$STATISTICS_GENERATOR_HOME/conf

#name of web home directory on server root
WEB_HOME=c2mon-statistics-web



#################
# CONFIGURATION #
#################

#log4j configuration file
LOG4J_CONF_FILE=$STATISTICS_GENERATOR_CONF_HOME/log4j.xml

#webpage chart configuration file
GRAPH_CONF_FILE=$STATISTICS_GENERATOR_CONF_HOME/webconfig.xml

# the directory to deploy to
#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!#
#!! the image and html subdirectories of this directory    !!#
#!! are deleted recursively at each run (if R option is    !!#
#!! present)! Handle with care!                            !!#
#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!#
DEPLOY_HOME=$STATISTICS_GENERATOR_HOME/charts                                  #/opt/apache-tomcat/webapps/$WEB_HOME

#directory name under which to save chart images
IMAGE_DIR=chart-images

#directory name under which to save chart html fragments
HTML_DIR=chart-html


##############
# LIBRAIRIES #
##############

#run the java package

cd $STATISTICS_GENERATOR_HOME/bin

export LOG4J_CONF_FILE    
export GRAPH_CONF_FILE
export IMAGE_DIR
export HTML_DIR
export DEPLOY_HOME
export WEB_HOME
export STATISTICS_GENERATOR_HOME

. C2MON-STATISTICS-GENERATOR.jvm >${STATISTICS_GENERATOR_LOG_HOME}/statistics-generator.out.log 2>&1 &
