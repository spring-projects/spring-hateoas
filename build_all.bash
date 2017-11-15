#!/usr/bin/env bash
#
# Convenient way to run ALL build profiles at once, before committing changes.
#
#

./mvnw clean package &&
./mvnw clean package -Pspring5-next
