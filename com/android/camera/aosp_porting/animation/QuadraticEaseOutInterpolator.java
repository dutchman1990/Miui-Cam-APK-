package com.android.camera.aosp_porting.animation;

import android.view.animation.Interpolator;

public class QuadraticEaseOutInterpolator implements Interpolator {
    public float getInterpolation(float f) {
        return (-f) * (f - 2.0f);
    }
}
