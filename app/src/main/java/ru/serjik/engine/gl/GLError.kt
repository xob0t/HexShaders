package ru.serjik.engine.gl

import android.opengl.GLES20
import ru.serjik.utils.SerjikLog

object GLError {
    @JvmStatic
    fun check(tag: String) {
        while (true) {
            val error = GLES20.glGetError()
            if (error == 0) return
            SerjikLog.log("$tag: glError $error")
        }
    }
}
