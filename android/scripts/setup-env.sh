#!/usr/bin/env bash
# One-time setup: writes scripts/.env.local with the right paths.
# Source this file before running build/install scripts, or use the helpers
# which source it automatically.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$ROOT_DIR/scripts/.env.local"

JAVA_HOME_PATH="/opt/homebrew/opt/openjdk@17"
ANDROID_SDK_PATH="/opt/homebrew/share/android-commandlinetools"

if [ ! -d "$JAVA_HOME_PATH" ]; then
  echo "[setup] OpenJDK 17 not found. Installing via Homebrew..."
  brew install openjdk@17
fi

if [ ! -d "$ANDROID_SDK_PATH" ]; then
  echo "[setup] Android command-line tools not found. Installing..."
  brew install android-commandlinetools
fi

if ! command -v adb >/dev/null 2>&1; then
  echo "[setup] adb not found. Installing platform-tools..."
  brew install android-platform-tools
fi

cat > "$ENV_FILE" <<EOF
export JAVA_HOME="$JAVA_HOME_PATH"
export ANDROID_HOME="$ANDROID_SDK_PATH"
export ANDROID_SDK_ROOT="$ANDROID_SDK_PATH"
export PATH="\$JAVA_HOME/bin:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools:\$PATH"
EOF

echo "[setup] Wrote $ENV_FILE"
echo ""
echo "Tools:"
echo "  Java:   $JAVA_HOME_PATH"
echo "  Android SDK: $ANDROID_SDK_PATH"
echo ""
echo "Done. From now on, scripts/build.sh and scripts/install.sh will pick this up automatically."
