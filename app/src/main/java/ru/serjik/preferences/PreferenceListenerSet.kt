package ru.serjik.preferences

class PreferenceListenerSet {
    private val listeners = mutableSetOf<PreferenceChangeListener>()

    fun clear() {
        listeners.clear()
    }

    fun addListener(listener: PreferenceChangeListener) {
        listeners.add(listener)
    }

    fun notifyListeners(entry: PreferenceEntry) {
        for (listener in listeners) {
            listener.onPreferenceChanged(entry)
        }
    }
}
