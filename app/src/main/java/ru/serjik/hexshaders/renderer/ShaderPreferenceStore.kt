package ru.serjik.hexshaders.renderer

import android.content.Context
import android.content.SharedPreferences
import ru.serjik.preferences.PreferenceStore

/**
 * PreferenceStore implementation backed by Android SharedPreferences.
 * Used to persist shader configuration per-shader or for the application store.
 */
class ShaderPreferenceStore(name: String, private val context: Context) : PreferenceStore {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(name, 0)

    /** Clears all stored preferences. */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    override fun put(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun get(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }
}
