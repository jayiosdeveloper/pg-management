#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [ -f "scripts/.env.local" ]; then
  # shellcheck disable=SC1091
  source scripts/.env.local
else
  echo "[build] scripts/.env.local not found — run scripts/setup-env.sh first."
  exit 1
fi

# Bootstrap gradle wrapper if missing (first run only).
if [ ! -f "gradlew" ] || [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
  if ! command -v gradle >/dev/null 2>&1; then
    echo "[build] System gradle not found. Installing temporarily via Homebrew..."
    brew install gradle
  fi
  echo "[build] Bootstrapping Gradle wrapper (one-time, ~150MB)..."
  gradle wrapper --gradle-version 8.9 --distribution-type bin
  chmod +x gradlew
fi

echo "[build] Java:   $(java -version 2>&1 | head -1)"
echo "[build] Building debug APK..."

./gradlew --no-daemon assembleDebug

APK="$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK" ]; then
  echo ""
  echo "[build] ✔ APK: $APK ($(du -h "$APK" | cut -f1))"
else
  echo "[build] ✘ Build finished but APK not found at $APK"
  exit 1
fi
