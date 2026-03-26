package ru.serjik.utils

fun interface FPSCallback {
    fun onFPSUpdate(counter: FPSCounter)
}
