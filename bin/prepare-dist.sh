#!/bin/bash

echo ":: pulling latest version of sources"
git pull

echo ":: compiling and assembling binary"
mvn clean install assembly:assembly

if [ "$1" == "" ] ; then
  echo "Missing parameter - client id. - preparing general package"
fi

DATE=`date +"%Y%m%d"`
DIR="dists/tigase-monitor"
if [ ! "$1" == "" ] ; then
  DIR="${DIR}_$1"
fi
DIR="${DIR}_$DATE"

rm -rf $DIR $DIR.tgz

mkdir -p $DIR
mkdir $DIR/etc
mkdir $DIR/libs
mkdir $DIR/sounds

cp target/tigase-monitor*jar-with-dependencies.jar $DIR/libs/tigase-monitor.jar
cp sounds/* $DIR/sounds/
cp bin/monitor.sh $DIR/

if [ "$1" == "" ] ; then
	cp bin/monitor.properties $DIR/etc/monitor.properties
else
	cp etc/${1}-init.properties $DIR/etc/monitor.properties
fi

tar -czf $DIR.tgz $DIR

if [ ! -d packs ] ; then
	mkdir packs
fi

mv -f $DIR.tgz packs/