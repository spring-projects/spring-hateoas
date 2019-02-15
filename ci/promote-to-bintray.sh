#!/bin/bash

set -e -u

apt-get update
apt-get install -y jq

buildName=$( cat spring-hateoas-artifactory/build-info.json | jq -r '.buildInfo.name' )
buildNumber=$( cat spring-hateoas-artifactory/build-info.json | jq -r '.buildInfo.number' )
groupId=$( cat spring-hateoas-artifactory/build-info.json | jq -r '.buildInfo.modules[0].id' | sed 's/\(.*\):.*:.*/\1/' )
version=$( cat spring-hateoas-artifactory/build-info.json | jq -r '.buildInfo.modules[0].id' | sed 's/.*:.*:\(.*\)/\1/' )
targetRepo="libs-release-local"

echo "Promoting ${buildName}/${buildNumber} to ${targetRepo}"

curl \
	-s \
	--connect-timeout 240 \
	--max-time 2700 \
	-u ${ARTIFACTORY_USERNAME}:${ARTIFACTORY_PASSWORD} \
	-H "Content-type:application/json" \
	-d "{\"sourceRepos\": [\"libs-release-local\"], \"targetRepo\" : \"spring-distributions\", \"async\":\"true\"}" \
	-f \
	-X \
	POST "https://repo.spring.io/api/build/distribute/${buildName}/${buildNumber}" > /dev/null || { echo "Failed to distribute" >&2; exit 1; }

echo "Waiting for artifacts to be published"

ARTIFACTS_PUBLISHED=false
WAIT_TIME=10
COUNTER=0

while [ $ARTIFACTS_PUBLISHED == "false" ] && [ $COUNTER -lt 120 ]; do

	result=$( curl -s https://api.bintray.com/packages/spring/jars/"${groupId}" )
	versions=$( echo "$result" | jq -r '.versions' )
	exists=$( echo "$versions" | grep "$version" -o || true )

	if [ "$exists" = "$version" ]; then
		ARTIFACTS_PUBLISHED=true
	fi
	
	COUNTER=$(( COUNTER + 1 ))
	sleep $WAIT_TIME

done
