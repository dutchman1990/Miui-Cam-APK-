package com.android.camera;

import android.graphics.Color;
import android.os.SystemClock;
import com.android.camera.effect.draw_mode.DrawBasicTexAttribute;
import com.android.camera.effect.draw_mode.FillRectAttribute;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.RawTexture;

public class CaptureAnimManager {
    private long mAnimStartTime;
    private int mAnimType;
    private int mDrawHeight;
    private int mDrawWidth;
    private float mX;
    private float mY;

    public void animateHold() {
        this.mAnimType = 2;
    }

    public void animateHoldAndSlide() {
        this.mAnimType = 1;
    }

    public void animateSlide() {
        if (this.mAnimType == 2) {
            this.mAnimType = 3;
            this.mAnimStartTime = SystemClock.uptimeMillis();
        }
    }

    public void clearAnimation() {
        this.mAnimType = 0;
    }

    public boolean drawAnimation(GLCanvas gLCanvas, CameraScreenNail cameraScreenNail, RawTexture rawTexture) {
        long uptimeMillis = SystemClock.uptimeMillis() - this.mAnimStartTime;
        if (this.mAnimType == 3 && uptimeMillis > 120) {
            return false;
        }
        if (this.mAnimType == 1 && uptimeMillis > 140) {
            return false;
        }
        int i = this.mAnimType;
        if (this.mAnimType == 1) {
            i = uptimeMillis < 20 ? 2 : 3;
            if (i == 3) {
                uptimeMillis -= 20;
            }
        }
        if (i == 2) {
            gLCanvas.draw(new DrawBasicTexAttribute(rawTexture, (int) this.mX, (int) this.mY, this.mDrawWidth, this.mDrawHeight));
        } else if (i != 3) {
            return false;
        } else {
            gLCanvas.draw(new DrawBasicTexAttribute(rawTexture, (int) this.mX, (int) this.mY, this.mDrawWidth, this.mDrawHeight));
            gLCanvas.draw(new FillRectAttribute(this.mX, this.mY, (float) this.mDrawWidth, (float) this.mDrawHeight, Color.argb(178, 0, 0, 0)));
        }
        return true;
    }

    public void startAnimation(int i, int i2, int i3, int i4) {
        this.mAnimStartTime = SystemClock.uptimeMillis();
        this.mDrawWidth = i3;
        this.mDrawHeight = i4;
        this.mX = (float) i;
        this.mY = (float) i2;
    }
}
