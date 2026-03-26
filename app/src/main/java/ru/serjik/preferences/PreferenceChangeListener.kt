package ru.serjik.preferences

fun interface PreferenceChangeListener {
    fun onPreferenceChanged(entry: PreferenceEntry)
}
