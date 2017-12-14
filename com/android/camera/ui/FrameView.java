package com.android.camera.ui;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public abstract class FrameView extends View implements FocusIndicator, Rotatable {
    private final boolean LOGV = true;
    protected boolean mIsBigEnoughRect;
    protected Matrix mMatrix = new Matrix();
    protected int mOrientation;
    protected boolean mPause;

    public FrameView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void clear() {
    }

    public boolean faceExists() {
        return false;
    }

    public abstract RectF getFocusRect();

    public boolean isNeedExposure() {
        return this.mIsBigEnoughRect;
    }

    public void pause() {
        this.mPause = true;
    }

    public void resume() {
        this.mPause = false;
    }

    public void setOrientation(int i, boolean z) {
        this.mOrientation = i;
        invalidate();
    }

    public void showFail() {
    }

    public void showStart() {
    }

    public void showSuccess() {
    }
}
