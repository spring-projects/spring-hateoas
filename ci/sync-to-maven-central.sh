#!/bin/bash

set -e -u

apt-get update
apt-get install -y jq

buildName=$( cat spring-hateoas-artifactory/build-info.json | jq -r '.buildInfo.name' )
buildNumber=$( cat spring-hateoas-artifactory/build-info.json | jq -r '.buildInfo.number' )
groupId=$( cat spring-hateoas-artifactory/build-info.json | jq -r '.buildInfo.modules[0].id' | sed 's/\(.*\):.*:.*/\1/' )
version=$( cat spring-hateoas-artifactory/build-info.json | jq -r '.buildInfo.modules[0].id' | sed 's/.*:.*:\(.*\)/\1/' )

echo "Syncing ${buildName}/${buildNumber} to Maven Central"
	curl \
			-s \
			--connect-timeout 240 \
			--max-time 2700 \
			-u ${BINTRAY_USERNAME}:${BINTRAY_API_KEY} \
			-H "Content-Type: application/json" -d "{ \"username\": \"${SONATYPE_USER_TOKEN}\", \"password\": \"${SONATYPE_PASSWORD_TOKEN}\"}" \
			-f \
			-X \
			POST "https://api.bintray.com/maven_central_sync/spring/jars/${groupId}/versions/${version}" > /dev/null || { echo "Failed to sync" >&2; exit 1; }

echo "Sync complete"
