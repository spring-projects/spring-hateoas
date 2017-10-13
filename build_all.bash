#!/usr/bin/env bash
#
# Convenient way to run ALL build profiles at once, before committing changes.
#
#

mvn clean package &&
mvn clean package -Pspring5-next
