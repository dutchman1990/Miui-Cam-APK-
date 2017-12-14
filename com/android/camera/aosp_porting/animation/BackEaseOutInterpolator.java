package com.android.camera.aosp_porting.animation;

import android.view.animation.Interpolator;

public class BackEaseOutInterpolator implements Interpolator {
    private final float mOvershot;

    public BackEaseOutInterpolator() {
        this(0.0f);
    }

    public BackEaseOutInterpolator(float f) {
        this.mOvershot = f;
    }

    public float getInterpolation(float f) {
        float f2 = this.mOvershot == 0.0f ? 1.70158f : this.mOvershot;
        f -= 1.0f;
        return ((f * f) * (((f2 + 1.0f) * f) + f2)) + 1.0f;
    }
}
