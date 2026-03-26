package ru.serjik.preferences.controllers

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import ru.serjik.preferences.PreferenceController
import ru.serjik.preferences.values.RGBValue

class RGBController : PreferenceController() {
    private lateinit var redLabel: TextView
    private lateinit var greenLabel: TextView
    private lateinit var blueLabel: TextView
    private lateinit var redSeekBar: SeekBar
    private lateinit var greenSeekBar: SeekBar
    private lateinit var blueSeekBar: SeekBar
    private lateinit var label: String

    private val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            updateLabels()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            preferenceEntry!!.set(
                RGBValue(
                    redSeekBar.progress,
                    greenSeekBar.progress,
                    blueSeekBar.progress
                ).toString()
            )
        }
    }

    private fun createChannelRow(seekBar: SeekBar, value: Int, textView: TextView): LinearLayout {
        seekBar.max = 100
        seekBar.progress = value
        seekBar.layoutParams = LinearLayout.LayoutParams(1, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f)
        textView.layoutParams = LinearLayout.LayoutParams(dp(48), ViewGroup.LayoutParams.WRAP_CONTENT)

        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(seekBar.paddingLeft, dp(8), dp(0), dp(4))
            addView(textView)
            gravity = Gravity.CENTER_VERTICAL
            addView(seekBar)
        }
    }

    private fun updateLabels() {
        redLabel.text = "R = ${redSeekBar.progress}"
        greenLabel.text = "G = ${greenSeekBar.progress}"
        blueLabel.text = "B = ${blueSeekBar.progress}"
    }

    override fun createView(params: Array<String>): View {
        label = params[0]
        val currentValue = RGBValue(preferenceEntry!!.get())

        redSeekBar = SeekBar(context)
        redLabel = TextView(context)
        val redRow = createChannelRow(redSeekBar, currentValue.r, redLabel)

        greenSeekBar = SeekBar(context)
        greenLabel = TextView(context)
        val greenRow = createChannelRow(greenSeekBar, currentValue.g, greenLabel)

        blueSeekBar = SeekBar(context)
        blueLabel = TextView(context)
        val blueRow = createChannelRow(blueSeekBar, currentValue.b, blueLabel)

        val titleView = TextView(context).apply {
            val defaultValue = RGBValue(preferenceEntry!!.getDefault())
            text = String.format(
                "%s (default R = %d, G = %d, B = %d)",
                label,
                Integer.valueOf(defaultValue.r),
                Integer.valueOf(defaultValue.g),
                Integer.valueOf(defaultValue.b)
            )
            setPadding(redSeekBar.paddingLeft, dp(12), redSeekBar.paddingRight, dp(8))
        }

        updateLabels()
        redSeekBar.setOnSeekBarChangeListener(seekBarChangeListener)
        greenSeekBar.setOnSeekBarChangeListener(seekBarChangeListener)
        blueSeekBar.setOnSeekBarChangeListener(seekBarChangeListener)

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(titleView)
            addView(redRow)
            addView(greenRow)
            addView(blueRow)
        }
    }
}
