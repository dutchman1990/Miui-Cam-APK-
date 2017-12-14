package com.android.camera;

import android.os.SystemClock;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.RawTexture;

public class SwitchAnimManager {
    private long mAnimStartTime;
    private float mExtScale = 1.0f;
    private Interpolator mInterpolator = new AccelerateInterpolator();
    private boolean mMoveBack;
    private boolean mNewPreview;
    private int mPreviewFrameLayoutWidth;
    private boolean mRealFirst;
    private boolean mRecurBlur;
    private int mReviewDrawingHeight;
    private int mReviewDrawingWidth;
    private int mReviewDrawingX;
    private int mReviewDrawingY;
    private int mSwitchState = 0;

    private void drawBlurTexture(GLCanvas gLCanvas, int i, int i2, int i3, int i4, CameraScreenNail cameraScreenNail, float f) {
        if (this.mRecurBlur) {
            cameraScreenNail.renderBlurTexture(gLCanvas);
        }
        gLCanvas.getState().pushState();
        if (this.mNewPreview) {
            gLCanvas.getState().setBlendAlpha(1.0f - f);
        }
        cameraScreenNail.drawBlurTexture(gLCanvas, this.mReviewDrawingX, this.mReviewDrawingY, this.mReviewDrawingWidth, this.mReviewDrawingHeight);
        gLCanvas.getState().popState();
    }

    private void drawRealTimeTexture(GLCanvas gLCanvas, int i, int i2, int i3, int i4, CameraScreenNail cameraScreenNail, float f) {
        if (this.mNewPreview) {
            gLCanvas.getState().pushState();
            gLCanvas.getState().setAlpha(f);
            cameraScreenNail.directDraw(gLCanvas, i, i2, i3, i4);
            gLCanvas.getState().popState();
        }
    }

    public void clearAnimation() {
        this.mAnimStartTime = 0;
        this.mRecurBlur = false;
    }

    public boolean drawAnimation(GLCanvas gLCanvas, int i, int i2, int i3, int i4, CameraScreenNail cameraScreenNail, RawTexture rawTexture) {
        return drawAnimationBlend(gLCanvas, i, i2, i3, i4, cameraScreenNail, rawTexture);
    }

    public boolean drawAnimationBlend(GLCanvas gLCanvas, int i, int i2, int i3, int i4, CameraScreenNail cameraScreenNail, RawTexture rawTexture) {
        boolean z = true;
        long uptimeMillis = SystemClock.uptimeMillis() - this.mAnimStartTime;
        float f = this.mRecurBlur ? 200.0f : 300.0f;
        if (((float) uptimeMillis) > f) {
            z = false;
            uptimeMillis = (long) f;
        }
        float interpolation = this.mInterpolator.getInterpolation(((float) uptimeMillis) / f);
        if (!z && this.mRecurBlur) {
            this.mRecurBlur = false;
        }
        drawRealTimeTexture(gLCanvas, i, i2, i3, i4, cameraScreenNail, interpolation);
        drawBlurTexture(gLCanvas, i, i2, i3, i4, cameraScreenNail, interpolation);
        return z;
    }

    public boolean drawPreview(GLCanvas gLCanvas, int i, int i2, int i3, int i4, RawTexture rawTexture) {
        float f = ((float) i) + (((float) i3) / 2.0f);
        float f2 = ((float) i2) + (((float) i4) / 2.0f);
        float f3 = 1.0f;
        if (this.mPreviewFrameLayoutWidth != 0) {
            f3 = ((float) i3) / ((float) this.mPreviewFrameLayoutWidth);
        } else {
            Log.e("SwitchAnimManager", "mPreviewFrameLayoutWidth is 0.");
        }
        float f4 = ((float) this.mReviewDrawingWidth) * f3;
        float f5 = ((float) this.mReviewDrawingHeight) * f3;
        int round = Math.round(f - (f4 / 2.0f));
        int round2 = Math.round(f2 - (f5 / 2.0f));
        float alpha = gLCanvas.getState().getAlpha();
        rawTexture.draw(gLCanvas, round, round2, Math.round(f4), Math.round(f5));
        gLCanvas.getState().setAlpha(alpha);
        return true;
    }

    public float getExtScaleX() {
        return this.mExtScale;
    }

    public float getExtScaleY() {
        return this.mExtScale;
    }

    public void restartPreview() {
        this.mNewPreview = true;
    }

    public void setPreviewFrameLayoutSize(int i, int i2) {
        this.mPreviewFrameLayoutWidth = i;
    }

    public void setReviewDrawingSize(int i, int i2, int i3, int i4) {
        boolean z = false;
        this.mReviewDrawingX = i;
        this.mReviewDrawingY = i2;
        this.mReviewDrawingWidth = i3;
        this.mReviewDrawingHeight = i4;
        this.mMoveBack = CameraSettings.isBackCamera();
        this.mNewPreview = false;
        if (((double) Math.abs((((float) this.mReviewDrawingHeight) / ((float) this.mReviewDrawingWidth)) - (((float) Util.sWindowHeight) / ((float) Util.sWindowWidth)))) < 0.02d) {
            z = true;
        }
        this.mRealFirst = z;
    }

    public void startAnimation() {
        this.mAnimStartTime = SystemClock.uptimeMillis();
        this.mRecurBlur = true;
    }

    public void startResume() {
        this.mAnimStartTime = SystemClock.uptimeMillis();
        this.mRecurBlur = false;
    }
}
