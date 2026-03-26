package ru.serjik.hexshaders.renderer.slideshow

import android.content.Context
import android.opengl.GLES20
import ru.serjik.engine.gl.ShaderProgram
import ru.serjik.engine.gl.Texture
import ru.serjik.utils.AssetsUtils
import java.nio.Buffer
import java.nio.FloatBuffer

/**
 * Per-hexagon shader program for the slideshow rendering mode.
 * Renders individual hexagons into a 256x256 render target texture,
 * supporting partial (batched) rendering across multiple frames.
 */
class SlideShowShaderHex(
    context: Context,
    shaderSource: String,
    textureNames: List<String>
) : ShaderProgram(shaderSource) {

    private val attribPos: Int
    private val attribTexPos: Int
    private val uniformOffset: Int
    private val uniformResolution: Int
    private val uniformGlobalTime: Int
    private val uniformTimeDelta: Int
    private val uniformFrame: Int
    private val uniformChannels = intArrayOf(-1, -1, -1)
    private val channelTextures = arrayOfNulls<Texture>(3)

    init {
        val assets = context.assets

        // Load channel textures
        for (i in textureNames.indices) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1 + i)
            channelTextures[i] = Texture(
                AssetsUtils.readBitmap("textures/${textureNames[i]}", assets), true
            )
            Texture.setFilter(GLES20.GL_LINEAR, GLES20.GL_LINEAR)
            Texture.setWrap(GLES20.GL_REPEAT, GLES20.GL_REPEAT)
            uniformChannels[i] = getUniformLocation("iChannel$i")
        }

        uniformResolution = getUniformLocation("iResolution")
        uniformGlobalTime = getUniformLocation("iGlobalTime")
        uniformTimeDelta = getUniformLocation("iTimeDelta")
        uniformFrame = getUniformLocation("iFrame")
        uniformOffset = getUniformLocation("u_offset")
        attribPos = getAttribLocation("a_pos")
        attribTexPos = getAttribLocation("t_pos")
    }

    /**
     * Draws a batch of hexagons into the render target.
     *
     * @param rtWidth      render target width
     * @param rtHeight     render target height
     * @param offset       wallpaper scroll offset
     * @param positions    interleaved vertex buffer
     * @param startIndex   first hexagon index in this batch
     * @param count        number of hexagons to draw in this batch
     * @param globalTime   accumulated global time
     * @param timeDelta    time delta for this frame
     * @param frameIndex   current frame index
     */
    fun draw(
        rtWidth: Float, rtHeight: Float, offset: Float,
        positions: FloatBuffer, startIndex: Int, count: Int,
        globalTime: Float, timeDelta: Float, frameIndex: Int
    ) {
        use()
        bindPositions(positions)
        GLES20.glUniform3f(uniformResolution, rtWidth, rtHeight, rtWidth / rtHeight)
        GLES20.glUniform1f(uniformGlobalTime, globalTime)
        GLES20.glUniform1f(uniformTimeDelta, timeDelta)
        GLES20.glUniform1i(uniformFrame, frameIndex)
        GLES20.glUniform1f(uniformOffset, offset)
        channelTextures[0]?.let {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
            it.bind()
        }
        channelTextures[1]?.let {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
            it.bind()
        }
        GLES20.glUniform1i(uniformChannels[0], 1)
        GLES20.glUniform1i(uniformChannels[1], 2)
        GLES20.glUniform1i(uniformChannels[2], 3)
        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glDrawArrays(GLES20.GL_POINTS, startIndex, count)
    }

    /**
     * Binds interleaved position buffer (position at offset 0, tex coords at offset 2, stride 16 bytes).
     */
    fun bindPositions(positions: FloatBuffer) {
        positions.position(0)
        GLES20.glVertexAttribPointer(attribPos, 2, GLES20.GL_FLOAT, false, 16, positions as Buffer)
        GLES20.glEnableVertexAttribArray(attribPos)
        positions.position(2)
        GLES20.glVertexAttribPointer(attribTexPos, 2, GLES20.GL_FLOAT, false, 16, positions as Buffer)
        GLES20.glEnableVertexAttribArray(attribTexPos)
    }
}
