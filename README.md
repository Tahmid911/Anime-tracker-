# Anime Tracker

A mobile-first Android anime tracker built with Kotlin and Jetpack Compose.

## Works without Google AI

- Search anime by name using the public Jikan/MyAnimeList catalog
- Add shows to a local Room database
- Track watch status and episode-by-episode progress
- Add personal ratings and notes
- View library, details, archives, statistics, and PDF/JSON/Markdown exports
- Fetch poster art from public anime catalog sources
- No Gemini key, AI Studio approval, Firebase AI setup, or server account is required

## Build

Open this repository in Android Studio and run the `app` configuration, or use:

```bash
./gradlew assembleDebug
```

The debug APK is created at `app/build/outputs/apk/debug/app-debug.apk`.

## Important note

Photo recognition is intentionally not used in this build because it depended on Google AI approval. The reliable replacement is title search, which keeps the tracker fully usable without an AI key.
