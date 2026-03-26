package ru.serjik.hexshaders.renderer

/**
 * Interface for hex shader rendering scenes.
 * Defines the lifecycle and rendering contract for both legacy and slideshow hex scenes.
 */
interface HexScene {

    /** Called when the surface dimensions change. */
    fun onSurfaceChanged(width: Int, height: Int)

    /** Called when visibility changes. */
    fun onVisibilityChanged(visible: Boolean)

    /**
     * Returns performance/debug info labels.
     * [0] = perf label, [1] = fps, [2] = points per frame, [3] = total point count
     */
    fun getInfo(): Array<String>

    /** Initializes the scene: loads shader preferences, compiles shaders, loads textures. */
    fun initialize()

    /** Draws a single frame. */
    fun drawFrame()
}
