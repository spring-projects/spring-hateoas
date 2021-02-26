#!/bin/bash

set -euo pipefail

RAW_VERSION=`MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw \
  org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate \
  -Dexpression=project.version -q -DforceStdout`

# Split things up
VERSION_PARTS=($RAW_VERSION)

# Grab the last part, which is the actual version number.
echo ${VERSION_PARTS[${#VERSION_PARTS[@]}-1]}

