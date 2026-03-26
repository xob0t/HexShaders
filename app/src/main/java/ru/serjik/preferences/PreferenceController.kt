package ru.serjik.preferences

import android.content.Context
import android.view.View

abstract class PreferenceController {
    @JvmField
    protected var preferenceEntry: PreferenceEntry? = null

    @JvmField
    protected var context: Context? = null

    @JvmField
    protected var view: View? = null

    private var density: Float = -1.0f

    protected fun dp(px: Int): Int = ((px * density) + 0.5f).toInt()

    fun getView(): View? = view

    protected abstract fun createView(params: Array<String>): View

    fun getPreferenceEntry(): PreferenceEntry? = preferenceEntry

    companion object {
        @JvmStatic
        fun create(typeName: String, params: Array<String>, entry: PreferenceEntry, context: Context): PreferenceController {
            val className = "ru.serjik.preferences.controllers.${typeName}Controller"
            try {
                val controller = Class.forName(className).getDeclaredConstructor().newInstance() as PreferenceController
                controller.preferenceEntry = entry
                controller.context = context
                controller.density = context.resources.displayMetrics.density
                controller.view = controller.createView(params)
                return controller
            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException("can't instantiate: $className")
            }
        }
    }
}
