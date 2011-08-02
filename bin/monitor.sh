#!/bin/bash

DYLD_LIBRARY_PATH="/Users/kobit/Downloads/yjp-8.0.15/bin/mac"
#PROFILER="-agentlib:yjpagent"
GC="-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:ParallelCMSThreads=2"
JAVA_OPTIONS="${GC} ${PROFILER} -d64 -Xms100M -Xmx3000m -Dcom.apple.macos.useScreenMenuBar=true"
CP=`ls -d libs/*.jar 2>/dev/null | tr '\n' :`
java -Xdock:name='Tigase Monitor' ${JAVA_OPTIONS} -cp tigase-monitor.jar:libs/'*' tigase.monitor.MonitorMain --init etc/monitor.properties
