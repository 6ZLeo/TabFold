#!/bin/sh
# Minimal wrapper launcher; wrapper library is licensed under Apache-2.0.
APP_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
if [ -n "$JAVA_HOME" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    JAVA_CMD=java
fi
exec "$JAVA_CMD" -classpath "$APP_DIR/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
