#!/bin/bash

#DYLD_LIBRARY_PATH="/Users/kobit/Downloads/yjp-8.0.15/bin/mac"
#PROFILER="-agentlib:yjpagent"
#GC="-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:ParallelCMSThreads=2"
#JAVA_OPTIONS="${GC} ${PROFILER} -Xms100M -Xmx3000m"
#CP=`ls -d libs/*.jar 2>/dev/null | tr '\n' :`
java ${JAVA_OPTIONS} -jar libs/tigase-monitor.jar --init etc/monitor.properties
