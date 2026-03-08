#!/bin/sh
#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0
#
APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

DEFAULT_JVM_OPTS="-Xmx512m -Xms256m"

warn () { echo "$*"; }
die () { echo; echo "$*"; echo; exit 1; }

# Determine APP_HOME
PRG="$0"
PRG_DIR=`dirname "$PRG"`
APP_HOME=`cd "$PRG_DIR" && pwd -P`

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

[ -x "$JAVACMD" ] || JAVACMD=`which java 2>/dev/null` || die "Cannot find java."

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
    "-Dorg.gradle.appname=$APP_BASE_NAME" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain "$@"
