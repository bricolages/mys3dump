#!/bin/sh

# /usr/libexec/java_home exists only in macOS.
if [ -e /usr/libexec/java_home ]; then
    export JAVA_HOME=$(/usr/libexec/java_home -v 11)
fi

if ! java -version 2>&1 | fgrep -q 'version "11.'
then
    echo "$0: error: java version 11 does not exist.  Install OpenJDK 11"
    exit 1
fi
