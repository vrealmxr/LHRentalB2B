# LH Rental B2B — Android

Native Android client (Kotlin + Jetpack Compose) for LH Rental's B2B
furniture/event-equipment rental portal, calling the existing legacy PHP
backend directly — see [docs/API.md](docs/API.md) for the API contract and
what's new on the server side vs. what's reused as-is.

## Status

v0.1.0 — builds and runs. Implemented: login, catalogue browse + search +
category filter, product detail, cart (local), checkout → order creation,
order history + detail, account + invoice download.

Not yet done: token refresh, offline caching, push notifications, tablet
layout, a real launcher icon (current one is a placeholder vector), English
string resources (Greek only right now, matching the portal's default
locale).

## Building

Needs JDK 17+ and the Android SDK (platform 34, build-tools 34.0.0).

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21   # or your JDK 17+
./gradlew assembleDebug
```

APK lands at `app/build/outputs/apk/debug/app-debug.apk`. Install on a
device/emulator with `adb install app/build/outputs/apk/debug/app-debug.apk`,
or open the project in Android Studio and hit Run.

## Project layout

```
app/src/main/java/gr/lhrental/b2b/
  data/model/      API response types (Moshi)
  data/network/    Retrofit + OkHttp + bearer-token interceptor
  data/local/      DataStore-backed token storage
  data/repo/       B2bRepository (API + result unwrapping), CartStore
  ui/screens/      one package per feature area
  ui/nav/          navigation graph + bottom bar
  ui/theme/        colors pulled from the live site's CSS
```

No DI framework — `LhB2bApplication` is a small hand-rolled service locator.
Fine at this size; revisit if the screen count grows a lot.
