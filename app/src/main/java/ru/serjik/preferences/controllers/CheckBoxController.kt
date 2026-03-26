package ru.serjik.preferences.controllers

import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import ru.serjik.preferences.PreferenceController
import ru.serjik.preferences.values.BooleanValue

class CheckBoxController : PreferenceController() {
    private val checkedChangeListener = { _: android.widget.CompoundButton, isChecked: Boolean ->
        preferenceEntry!!.set(BooleanValue(isChecked).toString())
    }

    override fun createView(params: Array<String>): View {
        val label = params[0]

        val checkBox = CheckBox(context).apply {
            isChecked = BooleanValue(preferenceEntry!!.get()).value
            text = label
            setPadding(paddingLeft, dp(12), paddingRight, dp(12))
            setOnCheckedChangeListener(checkedChangeListener)
        }

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(checkBox)
        }
    }
}
