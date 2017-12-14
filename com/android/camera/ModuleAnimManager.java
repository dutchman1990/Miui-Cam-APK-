package com.android.camera;

import android.graphics.Color;
import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import com.android.camera.effect.draw_mode.FillRectAttribute;
import com.android.gallery3d.ui.GLCanvas;

public class ModuleAnimManager {
    private float mAnimDuration;
    private long mAnimStartTime;
    private int mAnimState;
    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    public void animateStartHide() {
        this.mAnimState = 1;
        this.mAnimDuration = 300.0f;
        this.mAnimStartTime = SystemClock.uptimeMillis();
    }

    public void animateStartShow() {
        this.mAnimState = 3;
        this.mAnimDuration = 200.0f;
        this.mAnimStartTime = SystemClock.uptimeMillis();
    }

    public void clearAnimation() {
        this.mAnimState = 0;
        this.mAnimDuration = 0.0f;
    }

    public boolean drawAnimation(GLCanvas gLCanvas, int i, int i2, int i3, int i4) {
        long uptimeMillis = SystemClock.uptimeMillis() - this.mAnimStartTime;
        if (((float) uptimeMillis) > this.mAnimDuration) {
            if (this.mAnimState == 3) {
                this.mAnimState = 0;
                this.mAnimDuration = 0.0f;
                return false;
            } else if (this.mAnimState == 1) {
                this.mAnimState = 2;
            }
        }
        int i5 = 0;
        float f = this.mAnimDuration != 0.0f ? ((float) uptimeMillis) / this.mAnimDuration : 0.0f;
        switch (this.mAnimState) {
            case 1:
                i5 = (int) (this.mInterpolator.getInterpolation(f) * 240.0f);
                break;
            case 2:
                i5 = 240;
                break;
            case 3:
                i5 = (int) (this.mInterpolator.getInterpolation(1.0f - f) * 240.0f);
                break;
        }
        gLCanvas.draw(new FillRectAttribute((float) i, (float) i2, (float) i3, (float) i4, Color.argb(i5, 0, 0, 0)));
        return this.mAnimState != 2;
    }
}
