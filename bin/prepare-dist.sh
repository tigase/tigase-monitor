#!/bin/bash

DATE=`date +"%Y%m%d"`
DIR="tigase-monitor_$DATE"

rm -rf $DIR $DIR.tgz

mkdir $DIR
mkdir $DIR/etc
mkdir $DIR/libs
mkdir $DIR/sounds

cp target/tigase-monitor*.jar $DIR/tigase-monitor.jar
jar ufm $DIR/tigase-monitor.jar bin/manifest.txt
cp ../server/jars/tigase-server.jar $DIR/libs/
cp lib/*.jar $DIR/libs/
cp sounds/* $DIR/sounds/
cp bin/monitor.sh $DIR/
cp bin/monitor.properties $DIR/etc/

tar -czf $DIR.tgz $DIR
