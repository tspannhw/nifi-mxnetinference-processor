#!/bin/sh
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home
# Put in your JDK 8, otherwise maven can't run JUnits or skip them.
# Maven 3.6 + JDK 8 on OSX works fine
# Java 11 didn't like the tests
# mvn clean package -DskipTests
mvn package
