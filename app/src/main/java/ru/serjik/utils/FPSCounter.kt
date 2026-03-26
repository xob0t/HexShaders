package ru.serjik.utils

import android.os.SystemClock

class FPSCounter @JvmOverloads constructor(
    private val callback: FPSCallback? = null,
    private val callbackIntervalMillis: Int = 3000
) {
    private var deltaMillis: Int = 0
    private var smoothedDelta: Float = 0.0f
    private var lastTimestamp: Long = now()
    private var nextCallbackTime: Long = now() + callbackIntervalMillis

    fun tick() {
        val currentTime = now()
        deltaMillis = (currentTime - lastTimestamp).toInt()
        lastTimestamp = currentTime
        if (deltaMillis > 250) deltaMillis = 250
        if (deltaMillis < 1) deltaMillis = 1
        smoothedDelta += (deltaMillis - smoothedDelta) / 8.0f
        if (currentTime > nextCallbackTime) {
            nextCallbackTime = currentTime + callbackIntervalMillis
            callback?.onFPSUpdate(this)
        }
    }

    val fps: Float
        @JvmName("getFPS")
        get() = (1000.0 / smoothedDelta.toDouble()).toFloat()

    companion object {
        private fun now(): Long = SystemClock.elapsedRealtime()
    }
}
