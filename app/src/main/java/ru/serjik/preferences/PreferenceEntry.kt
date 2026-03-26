package ru.serjik.preferences

class PreferenceEntry(
    private val key: String,
    private val defaultValue: String,
    private val store: PreferenceStore
) {
    private val listeners = PreferenceListenerSet()

    fun getDefault(): String = defaultValue

    fun set(value: String) {
        if (get() == value) return
        store.put(key, value)
        listeners.notifyListeners(this)
    }

    fun get(): String = store.get(key, defaultValue)

    fun reset() {
        set(getDefault())
    }

    fun getListeners(): PreferenceListenerSet = listeners
}
