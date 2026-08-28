# MafiaApp Web

Web client for MafiaApp — a rating tracker for a Mafia board game club. Players are
ranked by MMR earned across games; admins record match results and the leaderboard
updates automatically.

**Live:** https://mafiaapp-819fd.web.app
**Android client:** https://github.com/JeffreyIhesinulo/MafiaMobileApp

Both clients share one Firebase backend and are independent of each other — neither
needs the other to be deployed or running.

## Features

- Email/password auth with mandatory email verification
- Admin approval queue — new accounts can't sign in until an admin approves them
- Leaderboard with MMR, rank tiers (IRON through higher tiers) and win/loss records
- Game creation: assign players to roles (Citizen, Sheriff, Mafia, Don), record the
  result, MMR is calculated and distributed automatically
- Game history with per-player MMR changes
- Player profiles with an MMR-over-time graph
- Activity feed with admin announcements

## Stack

- **UI:** Compose Multiplatform (Kotlin/JS target — not Wasm, see note below)
- **Backend:** Firebase Authentication + Cloud Firestore
- **Firebase SDK:** [GitLive Firebase Kotlin SDK](https://github.com/GitLiveApp/firebase-kotlin-sdk)
- **Navigation:** JetBrains multiplatform port of Jetpack Navigation Compose
- **Hosting:** Firebase Hosting

## Security model

Firebase web config values are not secrets — they ship in the browser bundle and are
safe in a public repo. Access control lives in Firestore Security Rules, not in the
client:

- Users can read their own document always; reading the player list requires an
  approved account, so an unapproved sign-up can't enumerate other players
- `isAdmin` and `approved` can only be written by an existing admin — a user creating
  their own document is constrained to `approved: false`, `isAdmin: false` and zeroed
  stats
- Username uniqueness is enforced by a `usernames/{name}` lock collection: `create`
  succeeds only if the document doesn't exist, which makes it atomic and free of the
  check-then-write race a query-based check would have
- Games and activity entries are writable by admins only; deletes are disabled outright

API keys are additionally restricted by HTTP referrer (web) and package name + SHA-1
(Android) in the Google Cloud Console.

## Why Kotlin/JS and not Kotlin/Wasm?

GitLive's Firebase SDK does not currently support the `wasmJs` target
([tracking issue](https://github.com/GitLiveApp/firebase-kotlin-sdk/issues/426)).
This project uses the standard Kotlin/JS Compose-for-Web target instead. It behaves
identically in the browser, including Safari/iOS, just with a slightly larger bundle
and marginally slower cold start than Wasm would give.

## Project structure

```
composeApp/
├── src/
│   ├── commonMain/kotlin/   # Shared UI screens, repositories, models
│   └── jsMain/
│       ├── kotlin/          # Entry point (main.kt) + Firebase init
│       └── resources/       # index.html
firestore.rules              # Security rules, deployed separately
```

## Setup

### 1. Prerequisites

- JDK 17+
- Node.js
- [Firebase CLI](https://firebase.google.com/docs/cli) (`npm install -g firebase-tools`)

### 2. Firebase configuration

This project needs its own Web App registered in the Firebase project (separate from
the Android app's registration). In the Firebase Console:

**Project Settings → General → Your apps → Add app → Web**

Copy the resulting config values into `composeApp/src/jsMain/kotlin/main.kt`:

```kotlin
private val firebaseOptions = FirebaseOptions(
    apiKey = "...",
    authDomain = "...",
    projectId = "...",
    storageBucket = "...",
    applicationId = "...",
    gcmSenderId = "..."
)
```

### 3. Build

```bash
./gradlew composeApp:jsBrowserDistribution
```

Output: `composeApp/build/dist/js/productionExecutable`

For local development with hot reload:

```bash
./gradlew composeApp:jsBrowserDevelopmentRun
```

Note: if you restrict the web API key by HTTP referrer, add your dev origin
(`http://localhost:8080/*`) to the allowed list or local runs will fail auth.

### 4. Deploy

```bash
firebase deploy --only hosting
firebase deploy --only firestore:rules
```

Deploy the client before the rules when rules get stricter — otherwise the live
bundle runs against rules it wasn't written for.

## Implementation notes

- The `<script>` tag in `index.html` must be placed inside `<body>` (or execution
  deferred until `window.onload`), otherwise `document.body` is `null` when
  Compose tries to attach to it.
- `GamePlayer` is `@Serializable` because GitLive's Firestore API relies on
  kotlinx.serialization for nested objects — plain `Map<String, Any?>` reads
  (which work fine on the Android SDK) don't work here.
- Firestore `Timestamp` fields need to be read via GitLive's `Timestamp` type and
  converted with `.toMilliseconds()`, not read directly as `Long`.
- Registration creates the auth account *before* querying Firestore, because the
  security rules require `request.auth != null`. On failure the account is rolled
  back with `user.delete()`.
