package ru.serjik.preferences

interface PreferenceStore {
    fun put(key: String, value: String)
    fun get(key: String, defaultValue: String): String
}
