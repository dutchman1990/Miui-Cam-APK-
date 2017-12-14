package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class TwoStateTextView extends TextView {
    private final float DISABLED_ALPHA;
    private boolean mFilterEnabled;

    public TwoStateTextView(Context context) {
        this(context, null);
    }

    public TwoStateTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.DISABLED_ALPHA = 0.4f;
        this.mFilterEnabled = true;
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        if (!this.mFilterEnabled) {
            return;
        }
        if (z) {
            setAlpha(1.0f);
        } else {
            setAlpha(0.4f);
        }
    }
}
