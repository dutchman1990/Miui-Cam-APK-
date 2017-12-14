package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class TwoStateImageView extends ImageView {
    private final float DISABLED_ALPHA;
    private boolean mFilterEnabled;
    private boolean mFilterInPressState;

    public TwoStateImageView(Context context) {
        this(context, null);
    }

    public TwoStateImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.DISABLED_ALPHA = 0.4f;
        this.mFilterEnabled = false;
        this.mFilterInPressState = true;
    }

    public void enableFilter(boolean z) {
        this.mFilterEnabled = z;
    }

    public void enablePressFilter(boolean z) {
        this.mFilterInPressState = z;
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

    public void setPressed(boolean z) {
        if (this.mFilterInPressState) {
            if (!this.mFilterInPressState) {
                return;
            }
            if (!isEnabled() && z) {
                return;
            }
        }
        super.setPressed(z);
    }
}
