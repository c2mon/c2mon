#!/bin/sh

#########################################################################
#                                                                       #
# This script is executed by the build server after the mvn build task  #
# is finished. The job of this script is to deploy the product (tar.gz) #
# file to the destination folder on the web server                      #
#                                                                       #
#########################################################################

HOST=timweb
USER=timtest
DEST_FOLDER=/tmp
TAR_FILE=./modules/tim2/build/c2mon-rule-composer-tim2-tim2.tar.gz

UNPACK_SCRIPT=./scripts/tim2-unpack.sh

echo "copying file ${TAR_FILE} to ${USER}@${HOST}:${DEST_FOLDER}"
scp ${TAR_FILE} ${USER}@${HOST}:${DEST_FOLDER}
echo "copying file ${UNPACK_SCRIPT} to ${USER}@${HOST}:${DEST_FOLDER}"
scp ${UNPACK_SCRIPT} ${USER}@${HOST}:${DEST_FOLDER}

#execute the unpack script on destination host and delete 
#the unpack script after
ssh ${USER}@${HOST} "/tmp/tim2-unpack.sh;rm -rf /tmp/tim2-unpack.sh"