#!/usr/bin/env bash
set -euo pipefail

PID_FILE="/tmp/selfevaluation-tomcat/tomcat.pid"

if [ ! -f "$PID_FILE" ]; then
  echo "Tomcat is not running"
  exit 0
fi

pid="$(cat "$PID_FILE")"
if kill -0 "$pid" 2>/dev/null; then
  kill "$pid"
  for _ in $(seq 1 10); do
    if ! kill -0 "$pid" 2>/dev/null; then
      rm -f "$PID_FILE"
      echo "Tomcat stopped"
      exit 0
    fi
    sleep 1
  done
  kill -9 "$pid" 2>/dev/null || true
fi

rm -f "$PID_FILE"
echo "Tomcat stopped"
