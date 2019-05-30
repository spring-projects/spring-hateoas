#!/bin/bash

set -euo pipefail

./mvnw -P${PROFILE} -Dmaven.test.skip=true clean deploy -B
