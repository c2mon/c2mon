#!/bin/bash

file=$1

currdir=`pwd`

if [ ! -f ${currdir}/${file} ] ; then
  echo "file needs to be specified"
  exit
fi

LINES_PER_FILE=40000

DTAGS=`cat $file | grep TAG_EQID | wc -l` 
SRULES=`cat $file | grep SR_ | wc -l`
CRULES=`cat $file | grep CR_ | wc -l`

prefix=`echo $file | awk -F'-' '{ print $1 }'`

dtagsFolder=${prefix}-dtags
srulesFolder=${prefix}-srules
crulesFolder=${prefix}-crules

if [ -d $currdir/$dtagsFolder ] ; then
  rm -rf ${dtagsFolder}
fi
mkdir ${dtagsFolder}

if [ -d $currdir/$srulesFolder ] ; then
  rm -rf ${srulesFolder}
fi
mkdir ${srulesFolder}

if [ -d $currdir/$crulesFolder ] ; then
  rm -rf ${crulesFolder}
fi
mkdir ${crulesFolder}

dtagsFile=${dtagsFolder}/${prefix}-dtags.sql
srulesFile=${srulesFolder}/${prefix}-srules.sql
crulesFile=${crulesFolder}/${prefix}-crules.sql

cat $file | grep TAG_EQID  > $dtagsFile
split -l ${LINES_PER_FILE} -d $dtagsFile ${dtagsFile}_
rm -f $dtagsFile

cat $file | grep SR_ > $srulesFile
split -l ${LINES_PER_FILE} -d $srulesFile ${srulesFile}_
rm -f $srulesFile

cat $file | grep CR_ > $crulesFile
split -l ${LINES_PER_FILE} -d $crulesFile ${crulesFile}_
rm -f $crulesFile

echo "number of dtags: $DTAGS"
echo "number of srules: $SRULES"
echo "number of crules: $CRULES"

