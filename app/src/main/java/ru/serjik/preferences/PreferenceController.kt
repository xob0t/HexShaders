package ru.serjik.preferences

import android.content.Context
import android.view.View
import ru.serjik.preferences.controllers.*

abstract class PreferenceController {
    var preferenceEntry: PreferenceEntry? = null
        internal set

    var context: Context? = null
        internal set

    var view: View? = null
        internal set

    private var density: Float = -1.0f

    protected fun dp(px: Int): Int = ((px * density) + 0.5f).toInt()

    protected abstract fun createView(params: Array<String>): View

    companion object {
        fun create(typeName: String, params: Array<String>, entry: PreferenceEntry, context: Context): PreferenceController {
            val controller = when (typeName) {
                "Range" -> RangeController()
                "Integer" -> IntegerController()
                "CheckBox" -> CheckBoxController()
                "RGB" -> RGBController()
                "Copyright" -> CopyrightController()
                "Separator" -> SeparatorController()
                else -> throw IllegalArgumentException("Unknown controller type: $typeName")
            }
            controller.preferenceEntry = entry
            controller.context = context
            controller.density = context.resources.displayMetrics.density
            controller.view = controller.createView(params)
            return controller
        }
    }
}
