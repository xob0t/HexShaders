package ru.serjik.engine.gl

import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

abstract class GLRenderer(surfaceView: GLSurfaceView) {

    private val surfaceView: GLSurfaceView
    private val handler = Handler(Looper.getMainLooper())
    private var frameDelayMillis = 16
    private val maxDeltaMillis = 250

    private val renderRequester: Runnable = object : Runnable {
        override fun run() {
            this@GLRenderer.surfaceView.requestRender()
            handler.postDelayed(this, frameDelayMillis.toLong())
        }
    }

    private val rendererImpl = object : GLSurfaceView.Renderer {
        private var frameCounter = 0
        private var needsResize = true
        private var viewWidth = 0
        private var viewHeight = 0
        private var lastTimestamp = SystemClock.elapsedRealtime()

        override fun onDrawFrame(gl: GL10) {
            if (needsResize) {
                frameCounter++
                if (frameCounter <= 3 || viewWidth <= 0 || viewHeight <= 0) return
                this@GLRenderer.onSurfaceChanged(viewWidth, viewHeight)
                needsResize = false
                return
            }
            val now = SystemClock.elapsedRealtime()
            var deltaMillis = (now - lastTimestamp).toInt()
            lastTimestamp = now
            if (deltaMillis < 0 || deltaMillis > maxDeltaMillis) {
                deltaMillis = maxDeltaMillis
            }
            this@GLRenderer.onDrawFrame(deltaMillis / 1000.0f)
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            needsResize = true
            frameCounter = 0
            viewWidth = width
            viewHeight = height
        }

        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            this@GLRenderer.onSurfaceCreated(config)
        }
    }

    init {
        onInit(surfaceView)
        this.surfaceView = surfaceView
        surfaceView.setEGLContextClientVersion(2)
        surfaceView.setRenderer(rendererImpl)
        surfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        handler.postDelayed(renderRequester, frameDelayMillis.toLong())
    }

    fun getSurfaceView(): GLSurfaceView = surfaceView

    protected abstract fun onDrawFrame(deltaSeconds: Float)

    fun setFrameDelay(millis: Int) {
        require(millis >= 0) { "frameDelayMillis = $millis" }
        frameDelayMillis = millis
    }

    protected abstract fun onSurfaceChanged(width: Int, height: Int)

    protected open fun onInit(surfaceView: GLSurfaceView) {}

    protected abstract fun onSurfaceCreated(config: EGLConfig)

    protected abstract fun onVisibilityChanged(visible: Boolean)

    fun resume() {
        onVisibilityChanged(true)
        handler.postDelayed(renderRequester, frameDelayMillis.toLong())
    }

    fun pause() {
        handler.removeCallbacks(renderRequester)
        onVisibilityChanged(false)
    }

    fun resetContext() {
        surfaceView.onPause()
        surfaceView.onResume()
    }
}
