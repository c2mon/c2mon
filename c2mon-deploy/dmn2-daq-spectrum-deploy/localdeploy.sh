#!/bin/bash
mvn package
mkdir -p deploy/log
rm i-f deploy/lib/*
cp ../c2mon-daq-spectrum/src/test/log4j.xml ./deploy/
cp ../c2mon-daq-spectrum/conf/daq-core-service.xml ./deploy/conf/
cp ../c2mon-daq-spectrum/conf/daq.properties ./deploy/conf/
cp /local/maven_local_repo/com/progress/sonic/*/*/*.jar deploy/lib/
tar zxvf build/dmn2-daq-spectrum-deploy-assembly.tar.gz -C deploy/
#rm deploy/lib/c2mon-daq-spectrum-*

