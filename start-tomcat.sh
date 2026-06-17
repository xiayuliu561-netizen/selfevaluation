#!/usr/bin/env bash
set -euo pipefail

CATALINA_BASE_DIR="/tmp/selfevaluation-tomcat"
CATALINA_HOME_DIR="/tmp/selfevaluation-runtime/apache-tomcat-9.0.117"
PID_FILE="$CATALINA_BASE_DIR/tomcat.pid"
LOG_FILE="/tmp/selfevaluation-run.log"
APP_WORK_DIR="$(cd "$(dirname "$0")" && pwd)"

if [ -f "$PID_FILE" ]; then
  old_pid="$(cat "$PID_FILE" || true)"
  if [ -n "$old_pid" ] && kill -0 "$old_pid" 2>/dev/null; then
    echo "Tomcat is already running: $old_pid"
    exit 0
  fi
  rm -f "$PID_FILE"
fi

cd "$APP_WORK_DIR"

setsid env \
  CATALINA_BASE="$CATALINA_BASE_DIR" \
  CATALINA_HOME="$CATALINA_HOME_DIR" \
  CATALINA_OPTS="-Duser.dir=$APP_WORK_DIR" \
  "$CATALINA_HOME_DIR/bin/catalina.sh" run \
  > "$LOG_FILE" 2>&1 < /dev/null &

echo $! > "$PID_FILE"
echo "Tomcat started: $(cat "$PID_FILE")"
echo "Log: $LOG_FILE"
