package ru.serjik.preferences.controllers

import android.view.View
import android.view.ViewGroup
import ru.serjik.preferences.PreferenceController

class SeparatorController : PreferenceController() {
    override fun createView(params: Array<String>): View {
        return View(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(8))
        }
    }
}
