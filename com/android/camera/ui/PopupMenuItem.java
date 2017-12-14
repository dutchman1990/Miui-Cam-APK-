package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class PopupMenuItem extends RelativeLayout implements Rotatable {
    private final float DISABLED_ALPHA = 0.4f;

    public PopupMenuItem(Context context) {
        super(context);
    }

    public PopupMenuItem(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public PopupMenuItem(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        if (z) {
            setAlpha(1.0f);
        } else {
            setAlpha(0.4f);
        }
    }

    public void setOrientation(int i, boolean z) {
        i = -i;
        int rotation = (int) getRotation();
        int i2 = (i >= 0 ? i % 360 : (i % 360) + 360) - (rotation >= 0 ? rotation % 360 : (rotation % 360) + 360);
        if (i2 == 0) {
            animate().cancel();
            return;
        }
        if (Math.abs(i2) > 180) {
            i2 = i2 >= 0 ? i2 - 360 : i2 + 360;
        }
        if (z) {
            animate().withLayer().rotation((float) (rotation + i2)).setDuration((long) ((Math.abs(i2) * 1000) / 270));
        } else {
            animate().withLayer().rotation((float) (rotation + i2)).setDuration(0);
        }
    }
}
