package ru.serjik.preferences.controllers

import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.View
import android.widget.TextView
import ru.serjik.preferences.PreferenceController

class CopyrightController : PreferenceController() {
    override fun createView(params: Array<String>): View {
        val url = preferenceEntry!!.getDefault()
        val html = "Thanks to <b>${params[0]}</b>. Used source code:<br><a href=\"$url\">$url</a>"

        return TextView(context).apply {
            text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            gravity = Gravity.CENTER
            movementMethod = LinkMovementMethod.getInstance()
        }
    }
}
