#!/bin/sh

# Install Node.js 18+ and Mockoon CLI: npm install --global @mockoon/cli
# From the project root, source .env and run: ./mock-server-mockoon/start.sh
: "${LOAD_BASE:?Source .env from the project root first}"
mockoon-cli start --data "$LOAD_BASE/mock-server-mockoon/mockoon.json" --hostname 127.0.0.1 --port 3000 --log-transaction
