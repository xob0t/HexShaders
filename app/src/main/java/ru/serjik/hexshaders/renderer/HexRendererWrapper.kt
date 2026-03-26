package ru.serjik.hexshaders.renderer

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import ru.serjik.engine.gl.GLRenderer
import ru.serjik.hexshaders.renderer.legacy.LegacyHexScene
import ru.serjik.hexshaders.renderer.slideshow.SlideShowHexScene
import ru.serjik.wallpaper.WallpaperOffsetsListener
import javax.microedition.khronos.egl.EGLConfig

/**
 * GLRenderer wrapper that delegates to either a LegacyHexScene or SlideShowHexScene
 * based on the GPU's vertex texture fetch capability (GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS).
 */
class HexRendererWrapper(
    surfaceView: GLSurfaceView,
    private val offsetsListener: WallpaperOffsetsListener
) : GLRenderer(surfaceView) {

    private val context: Context = surfaceView.context.applicationContext
    private var scene: HexScene? = null

    private fun getMaxVertexTextureUnits(): Int {
        val result = IntArray(1)
        GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, result, 0)
        return result[0]
    }

    override fun onDrawFrame(deltaSeconds: Float) {
        scene?.drawFrame()
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        scene?.onSurfaceChanged(width, height)
    }

    override fun onSurfaceCreated(config: EGLConfig) {
        setFrameDelay(16)
        if (scene == null) {
            scene = if (getMaxVertexTextureUnits() < 4) {
                LegacyHexScene(context)
            } else {
                SlideShowHexScene(context, offsetsListener)
            }
        }
        scene?.initialize()
    }

    override fun onVisibilityChanged(visible: Boolean) {
        scene?.onVisibilityChanged(visible)
    }

    /** Returns performance/debug info from the current scene. */
    fun getInfo(): Array<String> =
        scene?.getInfo() ?: arrayOf("", "", "", "")
}
