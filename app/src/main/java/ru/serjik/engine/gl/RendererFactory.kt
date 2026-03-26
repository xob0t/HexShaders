package ru.serjik.engine.gl

import android.opengl.GLSurfaceView

interface RendererFactory {
    fun createRenderer(surfaceView: GLSurfaceView): GLRenderer
}
