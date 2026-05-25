#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [ -f "scripts/.env.local" ]; then
  # shellcheck disable=SC1091
  source scripts/.env.local
else
  echo "[install] scripts/.env.local not found — run scripts/setup-env.sh first."
  exit 1
fi

# Build first
"$ROOT_DIR/scripts/build.sh"

APK="$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"
PKG="com.pg.management.debug"

# Make sure a device is connected
DEVICES=$(adb devices | grep -E "\sdevice$" | wc -l | tr -d ' ')
if [ "$DEVICES" -eq 0 ]; then
  echo ""
  echo "[install] ✘ No device detected. Connect your Android device via USB and accept the USB debugging prompt."
  echo "         Then verify with: adb devices"
  exit 1
fi

echo ""
echo "[install] Installing $APK on device..."
adb install -r "$APK"

echo "[install] Launching $PKG..."
adb shell am start -n "$PKG/com.pg.management.MainActivity"

echo "[install] ✔ Done."
