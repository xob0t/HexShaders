package ru.serjik.engine.gl

import android.opengl.GLES20
import ru.serjik.utils.SerjikLog

class RenderTarget(
    private var width: Int,
    private var height: Int,
    createFBO: Boolean
) {
    private var textureId: Int = 0
    private var framebufferId: Int = 0
    private val hasFBO: Boolean = createFBO
    private val tempIntArray = IntArray(1)
    private var hasBeenRenderedTo: Boolean = false

    init {
        if (createFBO) {
            GLES20.glGenTextures(1, tempIntArray, 0)
            textureId = tempIntArray[0]
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
            )

            GLES20.glGenFramebuffers(1, tempIntArray, 0)
            framebufferId = tempIntArray[0]
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId)
            GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, textureId, 0
            )

            val fbStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
            if (fbStatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                SerjikLog.log("Framebuffer incomplete, status: 0x${Integer.toHexString(fbStatus)}")
            }

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        }
    }

    fun bind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId)
        GLES20.glViewport(0, 0, width, height)
        if (!hasBeenRenderedTo) {
            hasBeenRenderedTo = true
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        }
    }

    fun getTextureId(): Int = if (hasBeenRenderedTo) textureId else 0

    fun getWidth(): Int = width

    fun getHeight(): Int = height

    /**
     * Releases the GL framebuffer and texture resources associated with this render target.
     * Must be called on the GL thread before discarding the object.
     */
    fun release() {
        if (hasFBO) {
            if (framebufferId != 0) {
                tempIntArray[0] = framebufferId
                GLES20.glDeleteFramebuffers(1, tempIntArray, 0)
                framebufferId = 0
            }
            if (textureId != 0) {
                tempIntArray[0] = textureId
                GLES20.glDeleteTextures(1, tempIntArray, 0)
                textureId = 0
            }
        }
        hasBeenRenderedTo = false
    }
}
