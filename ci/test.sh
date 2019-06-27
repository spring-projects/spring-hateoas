#!/bin/bash

set -euo pipefail

MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/spring-hateoas-maven-repository" ./mvnw -P${PROFILE} clean dependency:list test -Dsort -B
