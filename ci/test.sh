#!/bin/bash

set -euo pipefail

./mvnw -P${PROFILE} clean dependency:list test -Dsort -B
