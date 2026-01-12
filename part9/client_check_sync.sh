#!/bin/sh

curl -H "Content-Type: application/json" \
     -H "Accept: application/json, text/event-stream" \
     -d @client/find_file.json http://127.0.0.1:8080/mcp