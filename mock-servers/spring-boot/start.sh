#!/bin/sh

# Install Java 17+; the Maven wrapper downloads Maven automatically.
# From the project root, source .env and run: ./mock-servers/spring-boot/start.sh
: "${LOADBASE:?Source .env from the project root first}"
cd "$LOADBASE/mock-servers/spring-boot"
nohup ./mvnw spring-boot:run > spring-boot.log 2>&1 &
