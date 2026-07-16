# MafiaApp Web

Web client for MafiaApp — a Mafia board game club rating tracker.
Built with Compose Multiplatform (Kotlin/JS), sharing the same Firebase backend as the Android app.

## Stack

- **UI:** Compose Multiplatform (Kotlin/JS target — not Wasm, see note below)
- **Backend:** Firebase Authentication + Cloud Firestore
- **Firebase SDK:** [GitLive Firebase Kotlin SDK](https://github.com/GitLiveApp/firebase-kotlin-sdk)
- **Navigation:** JetBrains multiplatform port of Jetpack Navigation Compose
- **Hosting:** Firebase Hosting

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
│       ├── kotlin/           # Entry point (main.kt) + Firebase init
│       └── resources/        # index.html
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

> Firebase web config values are not secrets — they're safe to have in the browser's
> source code and in a public repo. Access control is enforced by Firestore Security
> Rules, not by hiding these values.

### 3. Build

```bash
./gradlew composeApp:jsBrowserDistribution
```

Output: `composeApp/build/dist/js/productionExecutable`

For local development with hot reload:

```bash
./gradlew composeApp:jsBrowserDevelopmentRun
```

### 4. Deploy

Set up `firebase.json` in the project root (not committed — see `.gitignore`):

```json
{
  "hosting": {
    "public": "composeApp/build/dist/js/productionExecutable",
    "ignore": ["firebase.json", "**/.*"],
    "rewrites": [{ "source": "**", "destination": "/index.html" }]
  }
}
```

```bash
firebase deploy --only hosting
```

## Notes

- This is a separate frontend from the Android app; both talk to the same Firebase
  backend. Neither depends on the other being deployed or running.
- The `<script>` tag in `index.html` must be placed inside `<body>` (or execution
  deferred until `window.onload`), otherwise `document.body` is `null` when
  Compose tries to attach to it.
- `GamePlayer` is `@Serializable` because GitLive's Firestore API relies on
  kotlinx.serialization for nested objects — plain `Map<String, Any?>` reads
  (which work fine on the Android SDK) don't work here.
- Firestore `Timestamp` fields need to be read via GitLive's `Timestamp` type and
  converted with `.toMilliseconds()`, not read directly as `Long`.
