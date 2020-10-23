#!/bin/bash

set -euo pipefail

MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw -P${PROFILE} -Dmaven.test.skip=true clean deploy -B
