#!/usr/bin/env bash
# Start the Book Rating API. Swagger UI opens automatically once it's up.
set -e
cd "$(dirname "$0")"
./mvnw spring-boot:run "$@"