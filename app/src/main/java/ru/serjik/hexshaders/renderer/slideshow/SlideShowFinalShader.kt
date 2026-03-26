package ru.serjik.hexshaders.renderer.slideshow

import android.content.res.AssetManager
import android.opengl.GLES20
import ru.serjik.engine.gl.ShaderProgram
import ru.serjik.engine.gl.Texture
import ru.serjik.utils.AssetsUtils
import java.nio.Buffer
import java.nio.FloatBuffer

/**
 * Final compositing shader for the slideshow hex renderer.
 * Combines the current and previous rendered textures with the hex point sprite texture
 * to produce the final output.
 */
class SlideShowFinalShader(assetManager: AssetManager) :
    ShaderProgram(AssetsUtils.readText("final_hex_slide_show.glsl", assetManager)) {

    private val uniformRenderedTexture1: Int = getUniformLocation("u_RenderedTexture1")
    private val uniformRenderedTexture2: Int = getUniformLocation("u_RenderedTexture2")
    private val uniformHexagonTexture: Int = getUniformLocation("u_HexagonTexture")
    private val uniformPointSize: Int = getUniformLocation("u_PointSize")
    private val uniformStep: Int = getUniformLocation("u_Step")
    private val attribPointPosition: Int = getAttribLocation("a_PointPosition")
    private val attribTexPointPosition: Int = getAttribLocation("t_PointPosition")
    private val hexTexture: Texture

    init {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
        hexTexture = Texture(AssetsUtils.readBitmap("textures/hex.png", assetManager), true)
        Texture.setFilter(GLES20.GL_LINEAR, GLES20.GL_LINEAR)
        Texture.setWrap(GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE)
    }

    /**
     * Draws the final composited frame.
     *
     * @param textureId1 texture ID from the first render target
     * @param textureId2 texture ID from the second render target
     * @param step       interpolation step (progress through hexagons)
     * @param pointSize  hex point sprite size
     * @param positions  interleaved vertex buffer (position + tex coords, stride 16)
     * @param count      number of hexagon points to draw
     */
    fun draw(
        textureId1: Int, textureId2: Int, step: Float,
        pointSize: Float, positions: FloatBuffer, count: Int
    ) {
        use()
        bindPositions(positions)

        // Bind hex texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
        hexTexture.bind()
        Texture.setFilter(GLES20.GL_LINEAR, GLES20.GL_LINEAR)
        Texture.setWrap(GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glUniform1i(uniformHexagonTexture, 2)

        // Bind rendered texture 1
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId1)
        Texture.setFilter(GLES20.GL_NEAREST, GLES20.GL_NEAREST)
        Texture.setWrap(GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glUniform1i(uniformRenderedTexture1, 0)

        // Bind rendered texture 2
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId2)
        Texture.setFilter(GLES20.GL_NEAREST, GLES20.GL_NEAREST)
        Texture.setWrap(GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glUniform1i(uniformRenderedTexture2, 1)

        GLES20.glUniform1f(uniformPointSize, pointSize)
        GLES20.glUniform1f(uniformStep, step)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE)
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, count)
    }

    /**
     * Binds interleaved position buffer (position at offset 0, tex coords at offset 2, stride 16 bytes).
     */
    fun bindPositions(positions: FloatBuffer) {
        positions.position(0)
        GLES20.glVertexAttribPointer(attribPointPosition, 2, GLES20.GL_FLOAT, false, 16, positions as Buffer)
        GLES20.glEnableVertexAttribArray(attribPointPosition)
        positions.position(2)
        GLES20.glVertexAttribPointer(attribTexPointPosition, 2, GLES20.GL_FLOAT, false, 16, positions as Buffer)
        GLES20.glEnableVertexAttribArray(attribTexPointPosition)
    }
}
