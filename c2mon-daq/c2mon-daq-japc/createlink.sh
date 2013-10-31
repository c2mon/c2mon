#!/bin/sh

file=$1

symlink=P_JAPC01-gen-dtags.xml
 
rm -rf $symlink

ln -s $file $symlink 
