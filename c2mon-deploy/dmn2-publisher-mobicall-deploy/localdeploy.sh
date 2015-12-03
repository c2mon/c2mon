#!/bin/bash
mvn package
mkdir -p deploy/log
tar zxvf build/c2mon-publisher-mobicall-assembly.tar.gz -C deploy/
rm deploy/lib/c2mon-publisher-mobicall-*

