#!/bin/bash
#
# TIM. CERN. All rights reserved.
#
#
# Author: Marta Ruiz Garcia
# Date:   2010-07-14
# 
# This script is used to automatically deploy all the libraries that the tim daq process needs at runtime.
# The script makes use of the deployment script developed by BE (http://wikis/display/Deployment/Home)to accomplish this task.
# The deployment script will download the dependencies of the daq process to a local installation directory and it will 
# create a start-up script that will be used to launch the application.
#
# This script is also in charge of creating a symlink pointing to the latest deployed version and another one pointing to the previous version.
# Notice that the name of the deployment INSTALLATION_FOLDER_NAME will automatically have the version number of the product appended to it. The installation location specified
# in the product.xml will just be a symlink to the latest installed product.
#
# ------Usage----------
# The script supports one argument
# -v Specifies the version number of the product to install. It also supports the PRO and NEXT aliases
#
# Ex. ./deploy-daq.sh -v NEXT
#
#
# 2010-09-21: adapted for TIM2 DAQ deployment - Mark Brightwell 

# Verbose output
#set -x

if [ "$1" == "-v" ] ; then
  PRODUCT_VERSION=$2	
else 
  PRODUCT_VERSION="PRO"
fi

PRODUCT_CONF_FILE=/user/pcrops/dist/tim2/tim2-daq/tim2-daq-core/${PRODUCT_VERSION}/product.xml

#Get the version number of the tim2-daq-core product
PRODUCT_DEF_LINE=`grep tim2-daq-core $PRODUCT_CONF_FILE` 
PRODUCT_VERSION=${PRODUCT_DEF_LINE#*version=\"}
PRODUCT_VERSION=${PRODUCT_VERSION%%\"*}

#Get the full LOCAL_REPOSITORY_PATH of the installation INSTALLATION_FOLDER_NAME specified in the product.xml.
PRODUCT_DEPLOYMENT_LINE=`grep installLocation $PRODUCT_CONF_FILE`
INSTALLATION_PATH=${PRODUCT_DEPLOYMENT_LINE#*installLocation=\"}
INSTALLATION_PATH=`eval echo ${INSTALLATION_PATH%\"*}`

#Get the application name
APPLICATION_DEF_LINE=`grep application $PRODUCT_CONF_FILE`
APPLICATION_NAME=${APPLICATION_DEF_LINE#*name=\"}
APPLICATION_NAME=${APPLICATION_NAME%%\"*}

#Get the name of the deployment INSTALLATION_FOLDER_NAME from the installation LOCAL_REPOSITORY_PATH. This name will be used to create a symlink to the latest product installation.
INSTALLATION_FOLDER_NAME=${INSTALLATION_PATH##*/}
LOCAL_REPOSITORY_PATH=${INSTALLATION_PATH%/*}
# echo LOCAL_REPOSITORY_PATH=${LOCAL_REPOSITORY_PATH}

#Build up the LOCAL_REPOSITORY_PATH of the installation INSTALLATION_FOLDER_NAME with the version number appended to the INSTALLATION_FOLDER_NAME name
LATEST_INSTALLATION=${LOCAL_REPOSITORY_PATH}/${INSTALLATION_FOLDER_NAME}\_${PRODUCT_VERSION}

if [ -e "$INSTALLATION_PATH" ] ; then
 if [ -L "$INSTALLATION_PATH" ] ; then
   #Get the LOCAL_REPOSITORY_PATH to the previous installed product version
   PREVIOUS_INSTALLATION=`ls -l ${INSTALLATION_PATH} | awk '{printf $11}'`
   if [ $PREVIOUS_INSTALLATION != $LATEST_INSTALLATION ] ; then
     #Create a symlink to the previous installed product version
     if [ -d "${LOCAL_REPOSITORY_PATH}/PREV" ] ; then
       if [ -L "${LOCAL_REPOSITORY_PATH}/PREV" ] ; then
         OBSOLETE_INSTALLATION=`ls -l ${LOCAL_REPOSITORY_PATH}/PREV | awk '{printf $11}'`
	 echo
         echo Removing obsolete build directory \'$OBSOLETE_INSTALLATION\'
	 echo
         rm ${LOCAL_REPOSITORY_PATH}/PREV
	 rm -Rf $OBSOLETE_INSTALLATION 
       else
         mv ${LOCAL_REPOSITORY_PATH}/PREV ${LOCAL_REPOSITORY_PATH}/PREV_bak
       fi
     fi
     echo
     echo Creating static link \'${LOCAL_REPOSITORY_PATH}/PREV\' to \'$PREVIOUS_INSTALLATION\'
     echo
     ln -s $PREVIOUS_INSTALLATION ${LOCAL_REPOSITORY_PATH}/PREV
   else
     echo
     echo "You are redeploying again the actual product version ==> The previous link is not overwritten"
     echo
   fi
 fi
fi

#Call the deployment script in charge of installing the product (downloads the product's dependencies and generates the start-up script)
echo Starting deployment ...
echo
deploy.py -d -f -p tim2-daq-core-runtime -a $APPLICATION_NAME -t $LATEST_INSTALLATION 
echo
echo ... deployment done!

# echo LATEST_INSTALLATION=${LATEST_INSTALLATION}
# echo INSTALLATION_PATH=${INSTALLATION_PATH}

#Create a symlink to the current installed product 
if [ -e ${INSTALLATION_PATH} ] ; then
 if [ -L "$INSTALLATION_PATH" ] ; then
   echo Removing static link ${INSTALLATION_PATH}
   rm $INSTALLATION_PATH
 else
   echo Moving {$INSTALLATION_PATH} to ${INSTALLATION_PATH}_bak
   mv $INSTALLATION_PATH ${INSTALLATION_PATH}_bak
 fi  
else
  echo "${INSTALLATION_PATH} does not yet exist ==> Nothing to move or to delete!"
fi
  
echo Recreating static link \'$INSTALLATION_PATH\' to \'$LATEST_INSTALLATION\'
ln -s $LATEST_INSTALLATION $INSTALLATION_PATH
