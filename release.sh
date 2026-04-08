#!/bin/bash
set -eu

while [[ "$#" -gt 0 ]]; do
  case "$1" in
    --signingKey=*) signingKey="${1#*=}";;
    --signingPassword=*) signingPassword="${1#*=}";;
    --sonatypeUsername=*) sonatypeUsername="${1#*=}";;
    --sonatypePassword=*) sonatypePassword="${1#*=}";;
  esac
  shift
done

if [ -z "${signingKey:-}${signingPassword:-}${sonatypeUsername:-}${sonatypePassword:-}" ]; then
  echo "Error: Missing required parameters"
  echo "Usage: $0 --signingKey=<signingKey> --signingPassword=<signingPassword> --sonatypeUsername=<sonatypeUsername> --sonatypePassword=<sonatypePassword>"
  exit 1
fi

./gradlew -PsigningKey="$signingKey" -PsigningPassword="$signingPassword" \
  -PsonatypeUsername="$sonatypeUsername" -PsonatypePassword="$sonatypePassword" \
  publishToSonatype closeAndReleaseSonatypeStagingRepository
