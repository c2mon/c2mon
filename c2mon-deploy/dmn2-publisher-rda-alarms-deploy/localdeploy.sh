#!/bin/bash
mvn package
mkdir -p deploy/log
tar zxvf build/c2mon-publisher-rda-alarms-deploy-assembly.tar.gz -C deploy/
rm deploy/lib/c2mon-publisher-rda-alarms-*
rm deploy/log4j.xml
#rm deploy/lib/slf4j-log4j12-1.7.5.jar

