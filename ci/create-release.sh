#!/bin/bash

set -euo pipefail

TICKET=$1
RELEASE=$2
SNAPSHOT=$3

# Bump up the version in pom.xml to the desired version and commit the change
./mvnw versions:set -DnewVersion=$RELEASE -DgenerateBackupPoms=false
git add .
git commit --message "$ISSUE - Releasing Spring HATEOAS v$RELEASE."

# Tag the release
git tag -s v$RELEASE -m "v$RELEASE"

# Bump up the version in pom.xml to the next snapshot
./mvnw versions:set -DnewVersion=$SNAPSHOT -DgenerateBackupPoms=false
git add .
git commit --message "$ISSUE - Continue development on v$SNAPSHOT."


