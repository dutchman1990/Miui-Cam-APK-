package com.android.camera;

import android.app.Activity;
import android.support.v7.recyclerview.C0049R;
import android.view.ViewGroup;
import android.widget.TextView;

public class OnScreenHint {
    private ViewGroup mHintView;

    public OnScreenHint(ViewGroup viewGroup) {
        this.mHintView = viewGroup;
    }

    public static OnScreenHint makeText(Activity activity, CharSequence charSequence) {
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(C0049R.id.on_screen_hint);
        OnScreenHint onScreenHint = new OnScreenHint(viewGroup);
        ((TextView) viewGroup.findViewById(C0049R.id.message)).setText(charSequence);
        return onScreenHint;
    }

    public void cancel() {
        Util.fadeOut(this.mHintView);
    }

    public int getHintViewVisibility() {
        return this.mHintView.getVisibility();
    }

    public void setText(CharSequence charSequence) {
        if (this.mHintView == null) {
            throw new RuntimeException("This OnScreenHint was not created with OnScreenHint.makeText()");
        }
        TextView textView = (TextView) this.mHintView.findViewById(C0049R.id.message);
        if (textView == null) {
            throw new RuntimeException("This OnScreenHint was not created with OnScreenHint.makeText()");
        }
        textView.setText(charSequence);
    }

    public void show() {
        Util.fadeIn(this.mHintView);
    }
}
