package ru.serjik.hexshaders.renderer.legacy

import android.content.Context
import android.opengl.GLES20
import ru.serjik.engine.gl.BufferAllocator
import ru.serjik.engine.gl.ShaderProgram
import ru.serjik.hexshaders.renderer.HexScene
import ru.serjik.hexshaders.renderer.ShaderPreferenceStore
import ru.serjik.preferences.PreferenceParser
import ru.serjik.preferences.values.IntegerValue
import ru.serjik.utils.AssetsUtils
import ru.serjik.utils.DeltaTimer
import ru.serjik.utils.FPSCounter
import ru.serjik.utils.HexUtils
import java.nio.FloatBuffer
import kotlin.math.abs

/**
 * Legacy hex scene that renders all hexagons in a single pass per frame.
 * Used on GPUs with fewer than 4 vertex texture units.
 */
class LegacyHexScene(private val context: Context) : HexScene {

    private val assets = context.assets
    private var pointSize = 0f
    private var pointsInRow = 0f
    private var hexCount = 0
    private var timeScale = 0f
    private var screenWidth = 0f
    private var screenHeight = 0f
    private lateinit var hexPositions: FloatBuffer
    private lateinit var shaderHex: LegacyShaderHex

    val deltaTimer = DeltaTimer(250)
    private val fpsCounter = FPSCounter(null, 500)
    var frameIndex = 0
    var wallpaperOffset = 0.0f

    /**
     * Computes hex grid positions and returns the number of hex points.
     */
    private fun computeHexGrid(width: Float, height: Float, pointsInRow: Float): Int {
        val aspectX: Float
        val aspectY: Float
        val cellSize = 2.0f / pointsInRow
        if (width < height) {
            aspectY = width / height
            pointSize = (0.9f * width) / pointsInRow
            aspectX = 1.0f
        } else {
            pointSize = (0.9f * height) / pointsInRow
            aspectX = height / width
            aspectY = 1.0f
        }
        val cols = ((0.7f * width) / pointSize).toInt() + 2
        val rows = ((0.7f * height) / pointSize).toInt() + 1
        hexPositions = BufferAllocator.createFloatBuffer((rows * 2 + 1) * 3 * (cols * 2 + 1))
        hexPositions.position(0)
        for (r in -rows..rows) {
            for (q in (-cols - r / 2)..(cols - r / 2)) {
                val x = HexUtils.hexX(q, r) * aspectX * cellSize
                val y = HexUtils.hexY(r) * aspectY * cellSize
                if (abs(x) < 1.0f + cellSize && abs(y) < 1.0f + cellSize) {
                    hexPositions.put(x)
                    hexPositions.put(y)
                }
            }
        }
        return hexPositions.position() / 2
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glViewport(0, 0, width, height)
        screenWidth = width.toFloat()
        screenHeight = height.toFloat()
        hexCount = computeHexGrid(width.toFloat(), height.toFloat(), pointsInRow)
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
            "ppf: $hexCount",
            "tpc: $hexCount"
        )
    }

    override fun initialize() {
        val appStore = ShaderPreferenceStore("application_store", context)
        if (appStore.get("reset_settings", "false") == "true") {
            appStore.clearAll()
        }
        appStore.put("reset_settings", "true")
        val selectedShader = appStore.get("selected_shader", "03. rainbow.gl2n").replace(".gl", ".el")
        val shaderStore = ShaderPreferenceStore(selectedShader, context.applicationContext)
        val shaderSource = AssetsUtils.readText("shaders/$selectedShader", assets)
        val textures = PreferenceParser.extractSection(shaderSource, "textures(", ",", ")")
        val prefMap = PreferenceParser.createPreferenceMap(
            PreferenceParser.extractPrefTokens(shaderSource), shaderStore
        )
        val substitutedSource = PreferenceParser.substitutePreferences(shaderSource, shaderStore)
        val pointsEntry = prefMap["pointsInTheRow"]
        pointsInRow = (pointsEntry?.let { IntegerValue(it.get()).value } ?: 15).toFloat()
        val timeScaleEntry = prefMap["timeScale"]
        timeScale = (timeScaleEntry?.let { IntegerValue(it.get()).value } ?: 50).toFloat()
        shaderHex = LegacyShaderHex(assets, substitutedSource, textures, timeScale)
        ShaderProgram.releaseCompiler()
        appStore.put("reset_settings", "false")
    }

    override fun drawFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        shaderHex.draw(
            deltaTimer.tick().deltaSeconds,
            screenWidth, screenHeight,
            pointSize, hexPositions, hexCount
        )
        fpsCounter.tick()
    }
}
