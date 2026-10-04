#!/bin/sh

# Install Node.js 18+ and Mockoon CLI: npm install --global @mockoon/cli
# From the project root, source .env and run: ./mock-servers/mockoon/start.sh
: "${LOAD_BASE:?Source .env from the project root first}"
nohup mockoon-cli start --data "$LOAD_BASE/mock-servers/mockoon/mockoon.json" --hostname 127.0.0.1 --port 3000 --log-transaction > mockoon.log 2>&1 &