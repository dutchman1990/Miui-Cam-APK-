package com.android.camera;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera.Area;
import android.support.v7.recyclerview.C0049R;
import java.util.List;

public abstract class FocusManagerAbstract {
    protected final int FOCUS_AREA_HEIGHT = CameraAppImpl.getAndroidContext().getResources().getDimensionPixelSize(C0049R.dimen.focus_area_height);
    protected final float FOCUS_AREA_SCALE = 1.0f;
    protected final int FOCUS_AREA_WIDTH = CameraAppImpl.getAndroidContext().getResources().getDimensionPixelSize(C0049R.dimen.focus_area_width);
    protected final float METERING_AREA_SCALE = 1.8f;
    protected boolean mCancelAutoFocusIfMove;
    protected int mDisplayOrientation;
    protected List<Area> mFocusArea;
    protected boolean mInitialized = false;
    protected Matrix mMatrix = new Matrix();
    protected List<Area> mMeteringArea;
    protected boolean mMirror;
    protected Matrix mPreviewChangeMatrix = new Matrix();
    protected int mPreviewHeight;
    protected int mPreviewWidth;
    protected int mRenderHeight;
    protected int mRenderWidth;
    protected int mState = 0;

    protected void calculateTapArea(int i, int i2, float f, int i3, int i4, int i5, int i6, Rect rect) {
        int i7 = (int) (((float) i) * f);
        int i8 = (int) (((float) i2) * f);
        int clamp = Util.clamp(i3 - (i7 / 2), 0, i5 - i7);
        int clamp2 = Util.clamp(i4 - (i8 / 2), 0, i6 - i8);
        RectF rectF = new RectF((float) clamp, (float) clamp2, (float) (clamp + i7), (float) (clamp2 + i8));
        this.mMatrix.mapRect(rectF);
        Util.rectFToRect(rectF, rect);
    }

    public void setDisplayOrientation(int i) {
        this.mDisplayOrientation = i;
        setMatrix();
    }

    protected void setMatrix() {
        if (this.mPreviewWidth != 0 && this.mPreviewHeight != 0) {
            Matrix matrix = new Matrix();
            Util.prepareMatrix(matrix, this.mMirror, this.mDisplayOrientation, this.mRenderWidth, this.mRenderHeight, this.mPreviewWidth / 2, this.mPreviewHeight / 2);
            matrix.invert(this.mMatrix);
            this.mPreviewChangeMatrix.reset();
            this.mPreviewChangeMatrix.postTranslate((float) ((-this.mPreviewWidth) / 2), (float) ((-this.mPreviewHeight) / 2));
            this.mPreviewChangeMatrix.postScale(0.6f, 0.6f);
            this.mPreviewChangeMatrix.postTranslate((float) (this.mPreviewWidth / 2), (float) (this.mPreviewHeight / 2));
            this.mInitialized = true;
        }
    }

    public void setMirror(boolean z) {
        this.mMirror = z;
        setMatrix();
    }

    public void setRenderSize(int i, int i2) {
        if (i != this.mRenderWidth || i2 != this.mRenderHeight) {
            this.mRenderWidth = i;
            this.mRenderHeight = i2;
            setMatrix();
        }
    }
}
