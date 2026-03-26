package ru.serjik.hexshaders.renderer

/**
 * Maps each shader program to its visually dominant colors.
 * These colors are derived from analyzing the GLSL source code of each shader
 * and identifying the primary, secondary, and tertiary color contributions.
 *
 * Used to provide accurate WallpaperColors to Android (API 27+) so the system
 * theme/accent matches the actual wallpaper appearance.
 */
object ShaderColors {

    data class ColorSet(
        @JvmField val primary: Int,
        @JvmField val secondary: Int,
        @JvmField val tertiary: Int
    )

    private val SHADER_COLORS = HashMap<String, ColorSet>().apply {
        // Original shaders (01-12)
        put("01. flame", ColorSet(0xFFFF8019.toInt(), 0xFF1980FF.toInt(), 0xFF1A0A00.toInt()))
        put("02. water", ColorSet(0xFF005990.toInt(), 0xFF003850.toInt(), 0xFF0A1A20.toInt()))
        put("03. rainbow", ColorSet(0xFFCC2244.toInt(), 0xFFAA1166.toInt(), 0xFF110022.toInt()))
        put("04. fire", ColorSet(0xFFFF6600.toInt(), 0xFFFFAA00.toInt(), 0xFF331100.toInt()))
        put("05. kalizyl", ColorSet(0xFF80B3FF.toInt(), 0xFFFFCC66.toInt(), 0xFF0A1020.toInt()))
        put("06. snow", ColorSet(0xFF738090.toInt(), 0xFF606878.toInt(), 0xFF2A2E33.toInt()))
        put("07. galaxy", ColorSet(0xFFCC3355.toInt(), 0xFF6622AA.toInt(), 0xFF2200CC.toInt()))
        put("08. light rays", ColorSet(0xFF336688.toInt(), 0xFF1A4455.toInt(), 0xFF0A1A22.toInt()))
        put("09. wave", ColorSet(0xFF33CC66.toInt(), 0xFF3366CC.toInt(), 0xFFCC3333.toInt()))
        put("10. sea waves", ColorSet(0xFF1A3038.toInt(), 0xFF6699CC.toInt(), 0xFFCCE6CC.toInt()))
        put("11. space", ColorSet(0xFF7733AA.toInt(), 0xFFCC8833.toInt(), 0xFF111133.toInt()))
        put("12. metaballs", ColorSet(0xFF3388AA.toInt(), 0xFF1A4D66.toInt(), 0xFF194D66.toInt()))

        // Premium HS20 shaders
        put("hs20. bacterium", ColorSet(0xFF33AA66.toInt(), 0xFF1A6644.toInt(), 0xFF0A2211.toInt()))
        put("hs20. cloud ten", ColorSet(0xFF6688BB.toInt(), 0xFF4466AA.toInt(), 0xFF223355.toInt()))
        put("hs20. digital brain", ColorSet(0xFF2288CC.toInt(), 0xFF115599.toInt(), 0xFF0A2244.toInt()))
        put("hs20. electric", ColorSet(0xFF4488FF.toInt(), 0xFF2266DD.toInt(), 0xFF112255.toInt()))
        put("hs20. hot shower", ColorSet(0xFFFF6633.toInt(), 0xFFCC4411.toInt(), 0xFF441100.toInt()))
        put("hs20. magnetismic", ColorSet(0xFF9933CC.toInt(), 0xFF6622AA.toInt(), 0xFF220044.toInt()))
        put("hs20. noise 3d fly through", ColorSet(0xFF4455AA.toInt(), 0xFF334488.toInt(), 0xFF111133.toInt()))
        put("hs20. perspex web", ColorSet(0xFF33BB88.toInt(), 0xFF228866.toInt(), 0xFF0A3322.toInt()))
        put("hs20. protean clouds", ColorSet(0xFF5577AA.toInt(), 0xFF334466.toInt(), 0xFF1A2233.toInt()))
        put("hs20. relentless", ColorSet(0xFFCC3322.toInt(), 0xFFAA2211.toInt(), 0xFF330A00.toInt()))
        put("hs20. spiral galaxy", ColorSet(0xFFCCAA77.toInt(), 0xFF887744.toInt(), 0xFF221100.toInt()))
        put("hs20. tiny clouds", ColorSet(0xFF5588CC.toInt(), 0xFF3366AA.toInt(), 0xFF112244.toInt()))
        put("hs20. zone alarm", ColorSet(0xFFDD4422.toInt(), 0xFFBB3311.toInt(), 0xFF440A00.toInt()))
    }

    private val DEFAULT_COLORS = ColorSet(0xFF333333.toInt(), 0xFF222222.toInt(), 0xFF111111.toInt())

    /**
     * Get the color set for a given shader filename.
     * Strips the file extension to match by base name.
     *
     * @param shaderName the shader asset filename, e.g. "03. rainbow.gl2n"
     * @return the matching ColorSet, or a neutral dark default if not found
     */
    @JvmStatic
    fun getColors(shaderName: String?): ColorSet {
        if (shaderName == null) return DEFAULT_COLORS

        // Strip extension (.gl2n, .gl2t, .gl3t, .el2n, etc.)
        var baseName = shaderName
        val dotIdx = shaderName.lastIndexOf('.')
        if (dotIdx > 0) {
            val prevDotIdx = shaderName.lastIndexOf('.', dotIdx - 1)
            if (prevDotIdx >= 0) {
                baseName = shaderName.substring(0, dotIdx)
            }
        }

        SHADER_COLORS[baseName]?.let { return it }

        // Fallback: try matching by prefix (number + name)
        for ((key, value) in SHADER_COLORS) {
            if (shaderName.startsWith(key)) {
                return value
            }
        }

        return DEFAULT_COLORS
    }
}
