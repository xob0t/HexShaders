# HexShaders Premium

Android live wallpaper that renders animated GLSL shader effects through a hexagonal grid of point sprites using OpenGL ES 2.0. Includes 25 shader effects with per-shader configuration (detail level, animation speed, colors).

Reverse-engineered from the [Google Play version](https://play.google.com/store/apps/details?id=ru.serjik.hexshaders.premium) (v2.2.2, versionCode 25). Original author: [KonyukhovSergey](https://github.com/KonyukhovSergey/HexShaders).

## Changes from the GP version

- Fully deobfuscated source (ProGuard → meaningful class/method names)
- Fixed wallpaper accent colors for Android 12+ (Material You) — system theme now matches the active shader instead of always being orange
- Modernized build: Gradle 8.12, AGP 8.8.2, compileSdk/targetSdk 36, Java 17

## Building

Requires Android SDK with platform 36 and JDK 17+.

```
./gradlew assembleDebug
```

APK will be at `build/outputs/apk/debug/HexShadersPremium-debug.apk`.

## License

[MIT](https://github.com/KonyukhovSergey/HexShaders/blob/master/LICENSE) (same as the original repo)
