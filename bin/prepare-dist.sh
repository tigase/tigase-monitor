#!/bin/bash

echo ":: pulling latest version of sources"
git pull

echo ":: compiling and assembling binary"
mvn clean package