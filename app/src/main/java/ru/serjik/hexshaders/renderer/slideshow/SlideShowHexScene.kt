package ru.serjik.hexshaders.renderer.slideshow

import android.content.Context
import android.opengl.GLES20
import ru.serjik.engine.gl.BufferAllocator
import ru.serjik.engine.gl.RenderTarget
import ru.serjik.engine.gl.ShaderProgram
import ru.serjik.hexshaders.renderer.HexScene
import ru.serjik.hexshaders.renderer.ShaderPreferenceStore
import ru.serjik.preferences.PreferenceParser
import ru.serjik.preferences.values.IntegerValue
import ru.serjik.utils.AssetsUtils
import ru.serjik.utils.DeltaTimer
import ru.serjik.utils.FPSCallback
import ru.serjik.utils.FPSCounter
import ru.serjik.utils.HexUtils
import ru.serjik.wallpaper.WallpaperOffsetsListener
import java.nio.FloatBuffer
import kotlin.math.abs

/**
 * Slideshow hex scene that incrementally renders hexagons across multiple frames.
 * Uses render-to-texture with triple buffering to distribute per-hexagon shader computation
 * across frames, achieving higher visual quality at the cost of temporal resolution.
 */
class SlideShowHexScene(
    private val context: Context,
    private val offsetsListener: WallpaperOffsetsListener
) : HexScene {

    private var globalTime = 0.0f

    private val assets = context.assets
    private var pointSize = 0f
    private var pointsInRow = 0
    private var totalHexCount = 0
    private var timeScale = 0f
    private lateinit var hexPositions: FloatBuffer
    private lateinit var shaderHex: SlideShowShaderHex
    private lateinit var finalShader: SlideShowFinalShader

    private val initLock = Any()
    private var screenTarget: RenderTarget? = null
    private var renderTargetA: RenderTarget? = null
    private var renderTargetB: RenderTarget? = null
    private var renderTargetC: RenderTarget? = null

    private val deltaTimer = DeltaTimer()
    private var currentHexIndex = 0
    private var hexesPerFrame = 1
    private var slideDivisor = 1

    /** FPS-adaptive callback that adjusts hexes-per-frame based on performance. */
    private val fpsAdaptiveCallback = FPSCallback { counter ->
        if (counter.fps < 20.0f) {
            hexesPerFrame = hexesPerFrame / 2 + 1
        } else {
            hexesPerFrame *= 2
            val maxPerFrame = totalHexCount / slideDivisor
            if (hexesPerFrame > maxPerFrame) {
                hexesPerFrame = maxPerFrame
            }
        }
    }

    private val fpsCounter = FPSCounter(fpsAdaptiveCallback, 500)
    var frameIndex = 0
    var wallpaperOffset = 0.0f
    var subFrameCount = 0

    /**
     * Computes hex grid positions with interleaved texture coordinates.
     * Each vertex has 4 floats: x, y, texU, texV (stride 16 bytes).
     */
    private fun computeHexGrid(width: Float, height: Float, pointsInRow: Float): Int {
        val aspectX: Float
        val aspectY: Float
        val cellSize = 2.0f / pointsInRow
        if (width < height) {
            aspectX = 1.0f
            aspectY = width / height
            pointSize = (0.9f * width) / pointsInRow
        } else {
            aspectX = height / width
            aspectY = 1.0f
            pointSize = (0.9f * height) / pointsInRow
        }
        val cols = ((0.7f * width) / pointSize).toInt() + 2
        val rows = ((0.7f * height) / pointSize).toInt() + 1
        hexPositions = BufferAllocator.createFloatBuffer((rows * 2 + 1) * 3 * (cols * 2 + 1))
        hexPositions.position(0)
        var index = 0
        for (r in -rows..rows) {
            for (q in (-cols - r / 2)..(cols - r / 2)) {
                val x = HexUtils.hexX(q, r) * aspectX * cellSize
                val y = HexUtils.hexY(r) * aspectY * cellSize
                if (abs(x) < 1.0f && abs(y) < 1.0f) {
                    hexPositions.put(x)
                    hexPositions.put(y)
                    hexPositions.put((index % 256 + 0.5f) / 256.0f)
                    hexPositions.put((index / 256 + 0.5f) / 256.0f)
                    index++
                }
            }
        }
        return hexPositions.position() / 4
    }

    /** Rotates the triple-buffered render targets. */
    private fun rotateRenderTargets() {
        val temp = renderTargetB
        renderTargetB = renderTargetC
        renderTargetC = renderTargetA
        renderTargetA = temp
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        totalHexCount = computeHexGrid(width.toFloat(), height.toFloat(), pointsInRow.toFloat())
        hexesPerFrame = totalHexCount / slideDivisor
        currentHexIndex = 0
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        // Release old render targets to prevent GPU memory leaks
        screenTarget?.release()
        renderTargetA?.release()
        renderTargetB?.release()
        renderTargetC?.release()
        screenTarget = RenderTarget(width, height, false)
        renderTargetA = RenderTarget(256, 256, true)
        renderTargetB = RenderTarget(256, 256, true)
        renderTargetC = RenderTarget(256, 256, true)
    }

    override fun onVisibilityChanged(visible: Boolean) {
        if (visible) {
            deltaTimer.tick()
        }
    }

    override fun getInfo(): Array<String> {
        val fps = fpsCounter.fps
        val perfLabel = when {
            fps < 20.0f -> "perf: drop"
            fps < 40.0f -> "perf: bad"
            fps < 55.0f -> "perf: good"
            else -> "perf: ok"
        }
        return arrayOf(
            perfLabel,
            "fps: %.1f".format(fps),
            "ppf: $hexesPerFrame",
            "tpc: $totalHexCount"
        )
    }

    override fun initialize() {
        val appStore = ShaderPreferenceStore("application_store", context)
        synchronized(initLock) {
            if (appStore.get("reset_settings", "false") == "true") {
                appStore.clearAll()
            }
            appStore.put("reset_settings", "true")
            val selectedShader = appStore.get("selected_shader", "03. rainbow.gl2n")
            val shaderStore = ShaderPreferenceStore(selectedShader, context.applicationContext)
            val shaderSource = AssetsUtils.readText("shaders/$selectedShader", assets)
            val textures = PreferenceParser.extractSection(shaderSource, "textures(", ",", ")")
            val prefMap = PreferenceParser.createPreferenceMap(
                PreferenceParser.extractPrefTokens(shaderSource), shaderStore
            )
            val substitutedSource = PreferenceParser.substitutePreferences(shaderSource, shaderStore)
            val pointsEntry = prefMap["pointsInTheRow"]
            pointsInRow = pointsEntry?.let { IntegerValue(it.get()).value } ?: 15
            val timeScaleEntry = prefMap["timeScale"]
            timeScale = (timeScaleEntry?.let { IntegerValue(it.get()).value } ?: 50).toFloat()
            if (prefMap.containsKey("slides")) {
                val fibonacciSequence = intArrayOf(1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144)
                var slidesIndex = IntegerValue(prefMap["slides"]!!.get()).value
                if (slidesIndex >= 12) {
                    slidesIndex = 4
                }
                slideDivisor = fibonacciSequence[slidesIndex]
            }
            shaderHex = SlideShowShaderHex(context.applicationContext, substitutedSource, textures)
            finalShader = SlideShowFinalShader(assets)
            ShaderProgram.releaseCompiler()
            appStore.put("reset_settings", "false")
        }
    }

    override fun drawFrame() {
        // Bind render target for per-hexagon shader computation
        renderTargetA!!.bind()
        if (currentHexIndex == 0) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        }

        // Determine batch size for this frame
        var batchSize = hexesPerFrame + 1
        if (currentHexIndex + batchSize > totalHexCount) {
            batchSize = totalHexCount - currentHexIndex
        }

        // Render batch of hexagons into 256x256 texture
        GLES20.glViewport(0, 0, 256, 256)
        shaderHex.draw(
            screenTarget!!.getWidth().toFloat(), screenTarget!!.getHeight().toFloat(),
            wallpaperOffset, hexPositions, currentHexIndex, batchSize,
            globalTime, deltaTimer.deltaSeconds * timeScale, frameIndex
        )

        // Bind screen and composite final output
        screenTarget!!.bind()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        finalShader.draw(
            renderTargetB!!.getTextureId(), renderTargetC!!.getTextureId(),
            currentHexIndex.toFloat() / totalHexCount, pointSize,
            hexPositions, totalHexCount
        )

        currentHexIndex += batchSize
        subFrameCount++

        // Check if all hexagons have been rendered for this cycle
        if (currentHexIndex >= totalHexCount) {
            wallpaperOffset = offsetsListener.getOffset()
            currentHexIndex = 0
            frameIndex++
            globalTime += deltaTimer.tick().deltaSeconds * timeScale
            if (abs(globalTime) > abs(60000.0f * timeScale) && abs(timeScale) > 0.01f) {
                globalTime = 0.0f
            }
            rotateRenderTargets()
        }
        fpsCounter.tick()
    }
}
