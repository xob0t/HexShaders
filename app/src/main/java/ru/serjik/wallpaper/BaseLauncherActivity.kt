package ru.serjik.wallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

abstract class BaseLauncherActivity : Activity() {

    private fun isWallpaperAlreadySet(): Boolean {
        val wm = WallpaperManager.getInstance(this)
        val info = wm.wallpaperInfo ?: return false
        if (settingsActivityClass.canonicalName == info.settingsActivity) {
            wm.forgetLoadedWallpaper()
            return true
        }
        return false
    }

    private fun openSettings() {
        startActivity(Intent(applicationContext, settingsActivityClass))
    }

    private fun setWallpaper() {
        try {
            val component = ComponentName(
                wallpaperServiceClass.`package`!!.name,
                wallpaperServiceClass.canonicalName!!
            )
            val intent = Intent("android.service.wallpaper.CHANGE_LIVE_WALLPAPER").apply {
                putExtra("android.service.wallpaper.extra.LIVE_WALLPAPER_COMPONENT", component)
            }
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            try {
                startActivity(Intent("android.service.wallpaper.LIVE_WALLPAPER_CHOOSER"))
            } catch (e2: ActivityNotFoundException) {
                try {
                    startActivity(Intent().apply {
                        action = "com.bn.nook.CHANGE_WALLPAPER"
                    })
                } catch (e3: ActivityNotFoundException) {
                    Toast.makeText(baseContext, "something goes wrong", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    protected open fun launch() {
        if (isWallpaperAlreadySet()) {
            openSettings()
        } else {
            setWallpaper()
        }
        finish()
    }

    protected abstract val settingsActivityClass: Class<*>
    protected abstract val wallpaperServiceClass: Class<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        launch()
    }
}
