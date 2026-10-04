#!/bin/sh

# Python 3 is required; no additional packages are needed.
# From the project root, source .env and run: ./mock-servers/python/start.sh
: "${LOAD_BASE:?Source .env from the project root first}"
nohup python3 "$LOAD_BASE/mock-servers/python/server.py" > python.log 2>&1 &
