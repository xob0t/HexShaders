package ru.serjik.wallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast

abstract class BaseSettingsActivity : Activity() {

    private val rootClickListener = View.OnClickListener { view ->
        view.setOnClickListener(null)
        view.isClickable = false
        showUI()
    }

    protected fun showUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.show(WindowInsets.Type.systemBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
        window.decorView.findViewWithTag<View>("ui").visibility = View.VISIBLE
    }

    /**
     * Notifies the wallpaper service to reload by writing a signal to SharedPreferences.
     * The service listens for changes via OnSharedPreferenceChangeListener.
     */
    private fun notifyServiceReload() {
        val prefs = getSharedPreferences("application_store", MODE_PRIVATE)
        prefs.edit()
            .putLong("reload_signal", System.currentTimeMillis())
            .apply()
    }

    private fun setupHideButton() {
        window.decorView.findViewWithTag<View>("button_hide_ui")?.setOnClickListener {
            hideUI()
        }
    }

    private fun setupSetWallpaperButton() {
        window.decorView.findViewWithTag<View>("button_set_wallpaper")?.apply {
            setOnClickListener {
                if (isTaskRoot) {
                    setAsWallpaper()
                }
                finish()
            }
            visibility = if (isWallpaperAlreadySet()) View.GONE else View.VISIBLE
        }
    }

    private fun isWallpaperAlreadySet(): Boolean {
        val wm = WallpaperManager.getInstance(this)
        val info = wm.wallpaperInfo ?: return false
        if (this::class.java.canonicalName == info.settingsActivity) {
            wm.forgetLoadedWallpaper()
            return true
        }
        return false
    }

    protected fun setAsWallpaper() {
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

    protected abstract val wallpaperServiceClass: Class<*>

    protected fun hideUI() {
        window.decorView.findViewWithTag<View>("root").apply {
            isClickable = true
            setOnClickListener(rootClickListener)
        }
        window.decorView.findViewWithTag<View>("ui").visibility = View.GONE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                hide(WindowInsets.Type.systemBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        }
    }

    override fun onPause() {
        super.onPause()
        notifyServiceReload()
    }

    override fun onResume() {
        super.onResume()
        setupSetWallpaperButton()
        setupHideButton()
    }
}
