#!/bin/sh

# Install Java 17+; the Maven wrapper downloads Maven automatically.
# From the project root, source .env and run: ./mock-servers/spring-boot/start.sh
: "${LOAD_BASE:?Source .env from the project root first}"
cd "$LOAD_BASE/mock-servers/spring-boot"
./mvnw spring-boot:run
