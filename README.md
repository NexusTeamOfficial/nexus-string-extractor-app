# Nexus String Extractor App

Android utility app (sideload only — **not for Play Store**).

## Features

- Pick / type Android `res/` folder path
- Optional `strings.xml` save path
- Dry-run & backup options
- Name prefix
- **MANAGE_EXTERNAL_STORAGE** (All files access) for Android 11–15
- Embedded NexusStringExtractor library — runs on device against project files on storage
- Jetpack: AppCompat, Material Components, ConstraintLayout

## Build APK

### Android Studio

1. Open this folder in Android Studio
2. Gradle sync
3. **Build → Build APK(s)**
4. Output: `app/build/outputs/apk/debug/app-debug.apk`

### CLI

```bash
./gradlew assembleDebug
```

## Install (sideload)

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## First launch

1. Tap **Grant permission** → enable **All files access**
2. Enter path to project `res` folder
3. Tap **Execute**

## Related

Library / CLI: [nexus-string-extractor](https://github.com/NexusTeamOfficial/nexus-string-extractor)

## License

MIT
