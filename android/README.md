# PG Management — Android App

Kotlin + Jetpack Compose Android app. Terminal-only build (no Android Studio needed).

This is **Phase 2** of the phased build. Auth (Splash, Login, Forgot Password, JWT persistence) is complete here; later phases add tenant management, room/bed allocation, billing, PDFs, notifications, etc.

---

## What you need on your Mac

The setup script below checks/installs these for you, but for reference:

- **Homebrew** — already installed for you
- **OpenJDK 17** — `brew install openjdk@17` (already installed)
- **Android Command Line Tools** — `brew install android-commandlinetools` (already installed)
- **Android Platform Tools (adb)** — `brew install android-platform-tools` (already installed)

The first build of the Gradle wrapper will download Gradle 8.9 (~150 MB). After that, builds are fully offline.

---

## One-time setup

```bash
cd android
chmod +x scripts/*.sh
./scripts/setup-env.sh
```

This writes a `.env.local` with the right paths. From then on you can use the convenience scripts in `scripts/`.

If you prefer to do it manually, set these env vars in your shell:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
```

Add to `~/.zshrc` if you want them permanent.

---

## Connect your Android device (one-time)

1. On your Android phone: **Settings → About phone → tap "Build number" 7 times** to unlock Developer options.
2. **Settings → Developer options → enable USB debugging**.
3. Plug the phone into your Mac with a USB cable.
4. On the phone, when prompted "Allow USB debugging from this computer?", tap **Always allow**.
5. Verify with:
   ```bash
   adb devices
   ```
   You should see your device listed (something like `RZ8N81F02NE  device`). If you see `unauthorized`, accept the prompt on the phone.

---

## Configure the API base URL

The app reads the API base URL from `local.properties`. Open `android/local.properties` and pick the right URL:

| Scenario                                  | Value                                |
|-------------------------------------------|--------------------------------------|
| Physical device on same Wi-Fi as your Mac | `http://YOUR_MAC_LAN_IP:4000/api/v1/` |
| Android emulator                          | `http://10.0.2.2:4000/api/v1/`        |
| Production (Render)                       | `https://your-api.onrender.com/api/v1/` |

To find your Mac's LAN IP:
```bash
ipconfig getifaddr en0   # Wi-Fi
```
e.g. `192.168.1.5` → set `API_BASE_URL=http://192.168.1.5:4000/api/v1/`.

> **Important:** for a real device, both the phone and the Mac must be on the **same Wi-Fi network**.

---

## Build the debug APK

From the `android/` folder:

```bash
./scripts/build.sh
```

or directly:

```bash
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

---

## Install on your device

```bash
./scripts/install.sh        # builds + installs + launches
```

or step by step:

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.pg.management.debug/com.pg.management.MainActivity
```

The app launches with the splash screen, then shows the Login screen. Log in with the admin credentials you created in Phase 1 (`SEED_ADMIN_EMAIL` + `SEED_ADMIN_PASSWORD` from `backend/.env`).

---

## Project structure

```
android/
├── app/                                    # main application module
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── kotlin/com/pg/management/
│       │   ├── PgApp.kt                   # Hilt Application class
│       │   ├── MainActivity.kt            # single Activity (Compose host)
│       │   ├── core/
│       │   │   ├── network/               # ApiEnvelope, ApiException, safeCall
│       │   │   ├── result/                # ApiResult sealed type
│       │   │   └── storage/               # DataStore-backed AuthStorage
│       │   ├── data/
│       │   │   ├── auth/                  # AuthRepositoryImpl + DTOs + AuthApi
│       │   │   └── common/                # AuthInterceptor, TokenAuthenticator
│       │   ├── domain/                    # repository interfaces + models
│       │   ├── di/                        # Hilt modules
│       │   └── ui/
│       │       ├── theme/                 # Material 3 dark theme (brand colors)
│       │       ├── components/            # GradientBackground, GlassCard, PrimaryButton
│       │       ├── navigation/            # NavGraph
│       │       └── screens/
│       │           ├── splash/
│       │           ├── login/
│       │           ├── forgot/
│       │           ├── session/           # shared logout VM
│       │           ├── admin/             # admin home (filled in Phase 3+)
│       │           └── tenant/            # tenant home (filled in Phase 7)
│       └── res/                           # themes, strings, icons, network config
├── gradle/
│   └── libs.versions.toml                 # version catalog (single source of truth)
├── scripts/
│   ├── setup-env.sh
│   ├── build.sh
│   └── install.sh
├── build.gradle.kts                       # root
├── settings.gradle.kts
└── gradle.properties
```

---

## Troubleshooting

- **`./gradlew: Permission denied`** — run `chmod +x gradlew` once.
- **`ANDROID_HOME is not set`** — run `./scripts/setup-env.sh` or export it manually.
- **Login fails with "Network error"** — your phone can't reach the backend. Confirm:
  - Backend is running (`curl http://YOUR_LAN_IP:4000/api/v1/health` from the Mac)
  - Phone and Mac are on the same Wi-Fi
  - `API_BASE_URL` in `local.properties` matches your Mac's LAN IP
  - Rebuild after editing `local.properties`
- **"Invalid credentials"** — make sure you ran `npm run seed:admin` in the backend folder and you're using the credentials it printed.
