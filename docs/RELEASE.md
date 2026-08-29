# Release signing & Play Store submission

## Where the keystore lives

The release signing key is **not** in this repo. It lives at:

```
~/keystores/lhrentalb2b-release.jks
~/keystores/lhrentalb2b-release.keystore.properties
```

on the machine that builds releases. `app/build.gradle.kts` looks for the
properties file at that path and only wires up the `release` signing config
if it finds it — a checkout without the keystore still builds a debug APK
fine, `bundleRelease`/`assembleRelease` just won't be signed.

**Never commit the `.jks` file or the `.keystore.properties` file.** Losing
the keystore means losing the ability to publish updates to the same Play
Store listing — back it up somewhere safe (password manager attachment or
encrypted backup), outside of git.

## Regenerating the keystore (only if you lose it — otherwise don't)

```bash
keytool -genkeypair -v \
  -keystore ~/keystores/lhrentalb2b-release.jks \
  -alias lhrentalb2b \
  -keyalg RSA -keysize 2048 -validity 10000
```

Note: modern `keytool` creates a PKCS12 keystore by default, and PKCS12
does **not** support a separate key password from the store password —
whatever you type for "key password" is silently ignored and the store
password is used for both. Put the *same* value in both `storePassword`
and `keyPassword` in the properties file to avoid confusion later.

`lhrentalb2b-release.keystore.properties` format:

```
storeFile=/Users/<you>/keystores/lhrentalb2b-release.jks
storePassword=<password>
keyAlias=lhrentalb2b
keyPassword=<same password>
```

## Building a release bundle

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab` — this is the
file you upload to Play Console (not an APK; Play Store wants an `.aab`).

## Publishing to Play Console (Internal Testing)

One-time setup:

1. Create a Google Play Console developer account at
   [play.google.com/console](https://play.google.com/console/) — $25
   one-time fee, identity verification required. **This step has to be
   done by whoever owns the LH Rental Google account**, not by an
   automated tool.
2. Create a new app entry (name, default language, app/game, free/paid).
3. Fill in the required **Privacy Policy** URL and **App content**
   questionnaire (data safety section) before any track will accept a
   build.
4. Go to **Testing → Internal testing** → create a release.
5. Upload `app-release.aab`.
6. Add tester email addresses (or a Google Group) under
   **Testers**, then share the generated opt-in link with them.

Every subsequent release: bump `versionCode`/`versionName` in
`app/build.gradle.kts`, `./gradlew bundleRelease`, upload the new `.aab` to
the same track.

## Parallel GitHub distribution

The GitHub Releases flow (debug APK, stable download link) documented in
the main [README](../README.md) is independent of Play Store distribution
and can keep running alongside it — useful for testers you don't want to
add to the Play Console tester list.
