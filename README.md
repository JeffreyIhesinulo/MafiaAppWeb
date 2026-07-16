# MafiaApp Web

Web version of MafiaApp built with Compose Multiplatform (Kotlin/Wasm).
Connects to the same Firebase backend as the Android app.

## Prerequisites

- JDK 17+
- Node.js (for webpack)
- Firebase CLI (`npm install -g firebase-tools`)

## Setup

### 1. Add a Web App to Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/project/mafiaapp-819fd/settings/general)
2. Scroll to "Your apps" section
3. Click "Add app" → Web (`</>`)
4. Register the app (name: "MafiaApp Web")
5. Copy the config values (apiKey, authDomain, appId, messagingSenderId)

### 2. Update Firebase Config

Open `composeApp/src/wasmJsMain/kotlin/main.kt` and replace the TODO values:

```kotlin
private val firebaseOptions = FirebaseOptions(
    apiKey = "AIzaSy...",                    // from Firebase Console
    authDomain = "mafiaapp-819fd.firebaseapp.com",
    projectId = "mafiaapp-819fd",
    storageBucket = "mafiaapp-819fd.appspot.com",
    applicationId = "1:458614595851:web:...",  // from Firebase Console
    messagingSenderId = "458614595851"          // from Firebase Console
)
```

### 3. Add Gradle Wrapper

Copy the `gradle/` directory (including `wrapper/gradle-wrapper.jar` and `gradle-wrapper.properties`)
and `gradlew`/`gradlew.bat` from your existing MafiaApp project or from the
`kotlin-wasm-compose-template` that's already on your Desktop.

```bash
cp -r ~/Desktop/kotlin-wasm-compose-template/gradle/wrapper/* gradle/wrapper/
cp ~/Desktop/kotlin-wasm-compose-template/gradlew .
cp ~/Desktop/kotlin-wasm-compose-template/gradlew.bat .
chmod +x gradlew
```

### 4. Build

```bash
./gradlew composeApp:wasmJsBrowserDistribution
```

### 5. Deploy to Firebase Hosting

```bash
cd composeApp/build/dist/wasmJs/productionExecutable
firebase deploy --only hosting
```

Or set up `firebase.json` in the project root:
```json
{
  "hosting": {
    "public": "composeApp/build/dist/wasmJs/productionExecutable",
    "ignore": ["firebase.json", "**/.*"],
    "rewrites": [{ "source": "**", "destination": "/index.html" }]
  }
}
```
Then: `firebase deploy --only hosting`

## Notes

- This is a separate project from the Android app. Both connect to the same Firebase backend.
- The Android app continues to work independently — this project doesn't affect it.
- Toast notifications are replaced with in-UI status messages (no Android context needed).
- The logo is a text placeholder. To use the real logo, place it in
  `composeApp/src/commonMain/composeResources/drawable/` and use Compose Multiplatform resources API.
- Firebase SDK: uses [GitLive Firebase Kotlin SDK](https://github.com/nicosama/firebase-kotlin-sdk)
  which provides multiplatform Firebase support. API is similar to Google's Android SDK but not identical.
- If GitLive doesn't support wasmJs natively, try building with JS compatibility mode:
  `./gradlew composeApp:composeCompatibilityBrowserDistribution`
