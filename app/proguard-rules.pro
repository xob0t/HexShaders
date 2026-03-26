# HexShadersPremium ProGuard Rules

# Keep wallpaper service (referenced from AndroidManifest.xml)
-keep class ru.serjik.hexshaders.premium.HexShadersService { *; }

# Keep WallpaperService engine inner classes
-keep class ru.serjik.wallpaper.GLWallpaperService$* { *; }
