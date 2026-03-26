package ru.serjik.hexshaders.renderer.slideshow;

import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import ru.serjik.engine.gl.ShaderProgram;
import ru.serjik.engine.gl.Texture;
import ru.serjik.utils.AssetsUtils;

/**
 * Per-hexagon shader program for the slideshow rendering mode.
 * Renders individual hexagons into a 256x256 render target texture,
 * supporting partial (batched) rendering across multiple frames.
 */
public class SlideShowShaderHex extends ShaderProgram {
    private AtomicInteger attribPos;
    private AtomicInteger attribTexPos;
    private AtomicInteger uniformOffset;
    private AtomicInteger uniformResolution;
    private AtomicInteger uniformGlobalTime;
    private AtomicInteger uniformTimeDelta;
    private AtomicInteger uniformFrame;
    private int[] uniformChannels;
    private Texture[] channelTextures;

    public SlideShowShaderHex(Context context, String shaderSource, List<String> textureNames) {
        super(shaderSource);
        this.attribPos = new AtomicInteger(-1);
        this.attribTexPos = new AtomicInteger(-1);
        this.uniformOffset = new AtomicInteger(-1);
        this.uniformResolution = new AtomicInteger(-1);
        this.uniformGlobalTime = new AtomicInteger(-1);
        this.uniformTimeDelta = new AtomicInteger(-1);
        this.uniformFrame = new AtomicInteger(-1);
        this.uniformChannels = new int[]{-1, -1, -1};
        this.channelTextures = new Texture[3];
        AssetManager assets = context.getAssets();

        // Load channel textures
        for (int i = 0; i < textureNames.size(); i++) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1 + i);
            this.channelTextures[i] = new Texture(AssetsUtils.readBitmap("textures/" + textureNames.get(i), assets), true);
            Texture.setFilter(GLES20.GL_LINEAR, GLES20.GL_LINEAR);
            Texture.setWrap(GLES20.GL_REPEAT, GLES20.GL_REPEAT);
            this.uniformChannels[i] = getUniformLocation("iChannel" + i);
        }

        this.uniformResolution.set(getUniformLocation("iResolution"));
        this.uniformGlobalTime.set(getUniformLocation("iGlobalTime"));
        this.uniformTimeDelta.set(getUniformLocation("iTimeDelta"));
        this.uniformFrame.set(getUniformLocation("iFrame"));
        this.uniformOffset.set(getUniformLocation("u_offset"));
        this.attribPos.set(getAttribLocation("a_pos"));
        this.attribTexPos.set(getAttribLocation("t_pos"));
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
    public void draw(float rtWidth, float rtHeight, float offset, FloatBuffer positions,
                     int startIndex, int count, float globalTime, float timeDelta, int frameIndex) {
        use();
        bindPositions(positions);
        GLES20.glUniform3f(this.uniformResolution.get(), rtWidth, rtHeight, rtWidth / rtHeight);
        GLES20.glUniform1f(this.uniformGlobalTime.get(), globalTime);
        GLES20.glUniform1f(this.uniformTimeDelta.get(), timeDelta);
        GLES20.glUniform1i(this.uniformFrame.get(), frameIndex);
        GLES20.glUniform1f(this.uniformOffset.get(), offset);
        if (this.channelTextures[0] != null) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            this.channelTextures[0].bind();
        }
        if (this.channelTextures[1] != null) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            this.channelTextures[1].bind();
        }
        GLES20.glUniform1i(this.uniformChannels[0], 1);
        GLES20.glUniform1i(this.uniformChannels[1], 2);
        GLES20.glUniform1i(this.uniformChannels[2], 3);
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDrawArrays(GLES20.GL_POINTS, startIndex, count);
    }

    /**
     * Binds interleaved position buffer (position at offset 0, tex coords at offset 2, stride 16 bytes).
     */
    public void bindPositions(FloatBuffer positions) {
        positions.position(0);
        GLES20.glVertexAttribPointer(this.attribPos.get(), 2, GLES20.GL_FLOAT, false, 16, (Buffer) positions);
        GLES20.glEnableVertexAttribArray(this.attribPos.get());
        positions.position(2);
        GLES20.glVertexAttribPointer(this.attribTexPos.get(), 2, GLES20.GL_FLOAT, false, 16, (Buffer) positions);
        GLES20.glEnableVertexAttribArray(this.attribTexPos.get());
    }
}
