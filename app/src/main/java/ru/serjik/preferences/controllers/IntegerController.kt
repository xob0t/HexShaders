package ru.serjik.preferences.controllers

import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import ru.serjik.preferences.PreferenceController
import ru.serjik.preferences.values.IntegerValue

class IntegerController : PreferenceController() {
    private lateinit var labelView: TextView
    private lateinit var label: String

    private val editorActionListener = TextView.OnEditorActionListener { textView, _, _ ->
        if (preferenceEntry!!.get() != textView.text.toString()) {
            preferenceEntry!!.set(textView.text.toString())
        }
        false
    }

    override fun createView(params: Array<String>): View {
        label = params[0]

        labelView = TextView(context).apply {
            text = label
            setPadding(dp(8), 0, dp(8), 0)
        }

        val editText = EditText(context).apply {
            text.clear()
            try {
                setText(IntegerValue(preferenceEntry!!.get()).toString())
            } catch (_: Exception) {
                setText(IntegerValue(preferenceEntry!!.getDefault()).toString())
            }
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
            setOnEditorActionListener(editorActionListener)
        }

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(dp(4), dp(8), dp(4), 0)
            addView(labelView)
            addView(editText)
        }
    }
}
