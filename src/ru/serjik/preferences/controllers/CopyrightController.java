package ru.serjik.preferences.controllers;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import ru.serjik.preferences.PreferenceController;

public class CopyrightController extends PreferenceController {
    @Override
    protected View createView(String[] params) {
        String url = this.preferenceEntry.getDefault();
        TextView textView = new TextView(this.context);
        textView.setText(Html.fromHtml("Thanks to <b>" + params[0] + "</b>. Used source code:<br><a href=\"" + url + "\">" + url + "</a>"));
        textView.setPadding(dp(12), dp(12), dp(12), dp(12));
        textView.setGravity(1);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        return textView;
    }
}
