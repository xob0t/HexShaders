package ru.serjik.utils

import android.os.SystemClock

class DeltaTimer @JvmOverloads constructor(
    private val maxDeltaMillis: Int = 0
) {
    private var rawDeltaMillis: Int = 0
    var deltaSeconds: Float = 0.0f
        private set
    private var lastTimestamp: Long = SystemClock.elapsedRealtime()

    fun tick(): DeltaTimer {
        val now = SystemClock.elapsedRealtime()
        rawDeltaMillis = (now - lastTimestamp).toInt()
        if (maxDeltaMillis > 0 && (rawDeltaMillis > maxDeltaMillis || rawDeltaMillis < 0)) {
            rawDeltaMillis = maxDeltaMillis
        }
        lastTimestamp = now
        deltaSeconds = rawDeltaMillis * 0.001f
        return this
    }
}
