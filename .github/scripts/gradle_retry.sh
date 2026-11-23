#!/usr/bin/env bash
set -uo pipefail

if [[ $# -eq 0 ]]; then
  echo "Usage: gradle_retry.sh <gradle-task> [gradle-args...]" >&2
  exit 2
fi

for attempt in 1 2 3; do
  echo "Gradle attempt ${attempt}/3: $*"
  ./gradlew "$@" --no-daemon --stacktrace
  status=$?
  if [[ $status -eq 0 ]]; then
    exit 0
  fi
  if [[ $attempt -lt 3 ]]; then
    delay=$((attempt * 15))
    echo "Gradle attempt failed (exit ${status}); retrying in ${delay}s." >&2
    sleep "$delay"
  else
    exit "$status"
  fi
done
