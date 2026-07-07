#!/usr/bin/env bash
set -euo pipefail

# Detect OS and run appropriate jpackage/gradle wrapper
OS="$(uname -s)"
echo "Detected OS: $OS"

# Allow passing extra args
EXTRA_ARGS="$@"

if [[ "$OS" == MINGW* || "$OS" == CYGWIN* || "$OS" == MSYS* || "$OS" == Windows_NT ]]; then
  echo "Running Windows gradle wrapper"
  ./gradlew.bat jpackage $EXTRA_ARGS
else
  echo "Running Unix gradle wrapper"
  ./gradlew jpackage $EXTRA_ARGS
fi
