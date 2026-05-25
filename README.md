# PG / Hostel Management ERP

Complete PG / Hostel management system: Android app (Kotlin + Jetpack Compose) + Node.js backend (Express + Supabase). Single monorepo.

## Structure

| Folder      | Description                                                                                          |
|-------------|------------------------------------------------------------------------------------------------------|
| [backend/](backend/)  | Node.js + Express REST API. Supabase PostgreSQL DB, Cloudinary storage, FCM notifications, JWT auth. |
| [android/](android/)  | Android app. Kotlin + Jetpack Compose, MVVM + Clean Architecture, Hilt DI, Retrofit, Room, DataStore. |

Each folder has its own README with detailed setup instructions.

## Phased build status

| Phase | Scope                                                          | Status      |
|-------|----------------------------------------------------------------|-------------|
| 1     | Backend: Express scaffold, full Supabase schema, JWT auth      | ✅ Complete |
| 2     | Android: project setup, splash, login, forgot-password, JWT    | ✅ Complete |
| 3     | Admin tenant management (CRUD + Cloudinary photo/Aadhaar)      | Pending     |
| 4     | Room + bed management                                          | Pending     |
| 5     | Billing (rent, food, cleaning, repair, electricity, water, …)  | Pending     |
| 6     | PDF invoices                                                   | Pending     |
| 7     | Tenant-side screens (profile, bills, history, notices)         | Pending     |
| 8     | Notifications (FCM + in-app)                                   | Pending     |
| 9     | Complaints, visitors, QR, entry logs, analytics                | Pending     |
| 10    | Polish (animations, glassmorphism, charts, release signing)    | Pending     |

## Quick start

```bash
# Backend
cd backend
cp .env.example .env       # fill in Supabase keys + JWT secrets
npm install
npm run migrate            # creates 12 tables in Supabase
npm run seed:admin         # creates first admin user
npm run dev                # starts on :4000

# Android (in another terminal)
cd ../android
chmod +x scripts/*.sh
./scripts/setup-env.sh
# edit local.properties → set API_BASE_URL to your Mac's LAN IP
./scripts/install.sh       # builds + installs APK on connected device
```

Full step-by-step setup is in each subfolder's README.

## Stack

- **Mobile:** Kotlin, Jetpack Compose, Material 3, Hilt, Retrofit, OkHttp, DataStore, Room, Navigation Compose
- **Backend:** Node.js 18+, Express 4, Joi, JWT, bcrypt
- **Database:** Supabase (PostgreSQL 15)
- **Storage:** Cloudinary (free tier)
- **Push:** Firebase Cloud Messaging (free tier)
- **Hosting:** Render (free tier)

All free / open source.

## Deployment

The backend is designed to deploy to **Render** (free tier). When configuring the Render Web Service:

- **Root Directory:** `backend`
- **Build Command:** `npm install`
- **Start Command:** `npm start`
- **Runtime:** Node
- **Environment Variables:** copy each line from your `backend/.env` (except `PORT` — Render auto-injects it)
