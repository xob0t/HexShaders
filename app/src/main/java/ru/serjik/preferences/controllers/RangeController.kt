package ru.serjik.preferences.controllers

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import ru.serjik.preferences.PreferenceController
import ru.serjik.preferences.values.IntegerValue

class RangeController : PreferenceController() {
    private lateinit var labelView: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var label: String
    private var minValue: Int = 0

    private val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            updateLabel(seekBar)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            preferenceEntry!!.set(IntegerValue(getSeekBarValue(seekBar)).toString())
        }
    }

    private fun getSeekBarValue(seekBar: SeekBar): Int = seekBar.progress + minValue

    private fun updateLabel(seekBar: SeekBar) {
        labelView.text = String.format(
            "%s = %d (default = %s)",
            label,
            Integer.valueOf(getSeekBarValue(seekBar)),
            preferenceEntry!!.getDefault()
        )
    }

    override fun createView(params: Array<String>): View {
        label = params[0]
        minValue = params[1].toInt()
        val maxValue = params[2].toInt()

        seekBar = SeekBar(context).apply {
            max = maxValue - minValue
            setPadding(paddingLeft, dp(8), paddingRight, dp(12))
            progress = IntegerValue(preferenceEntry!!.get()).value - minValue
        }

        labelView = TextView(context).apply {
            text = label
            setPadding(seekBar.paddingLeft, dp(12), seekBar.paddingRight, dp(8))
        }

        updateLabel(seekBar)
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener)

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(labelView)
            addView(seekBar)
        }
    }
}
