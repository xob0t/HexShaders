package ru.serjik.hexshaders.premium

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.View
import ru.serjik.wallpaper.BaseLauncherActivity

/**
 * Launcher activity for HexShaders Premium.
 * Checks if the wallpaper is already set and either opens settings or prompts to set it.
 */
class HexShadersActivity : BaseLauncherActivity() {

    override fun launch() {
        super.launch()
    }

    override val settingsActivityClass: Class<*>
        get() = HexShadersSettings::class.java

    /** Opens the Play Store page for this app (called from XML onClick). */
    fun buttonOpenPlayStoreClick(@Suppress("UNUSED_PARAMETER") view: View) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=ru.serjik.hexshaders.premium")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=ru.serjik.hexshaders.premium")))
        }
    }

    override val wallpaperServiceClass: Class<*>
        get() = HexShadersService::class.java
}
