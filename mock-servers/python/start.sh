#!/bin/sh

# Python 3 is required; no additional packages are needed.
# From the project root, source .env and run: ./mock-servers/python/start.sh
: "${LOADBASE:?Source .env from the project root first}"
nohup python3 "$LOADBASE/mock-servers/python/server.py" > python.log 2>&1 &
