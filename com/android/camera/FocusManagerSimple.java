package com.android.camera;

import android.graphics.Rect;
import android.hardware.Camera.Area;
import java.util.ArrayList;
import java.util.List;

public class FocusManagerSimple extends FocusManagerAbstract {
    public FocusManagerSimple(int i, int i2, boolean z, int i3) {
        this.mDisplayOrientation = i3;
        this.mMirror = z;
        setPreviewSize(i, i2);
        this.mInitialized = true;
    }

    public boolean canAutoFocus() {
        return this.mInitialized && this.mState != 2;
    }

    public boolean canRecord() {
        if (this.mState != 1 && this.mState != 2) {
            return true;
        }
        this.mState = 2;
        return false;
    }

    public void cancelAutoFocus() {
        this.mState = 0;
        this.mCancelAutoFocusIfMove = false;
    }

    public void focusPoint() {
        this.mState = 1;
        this.mCancelAutoFocusIfMove = false;
    }

    public int getDefaultFocusAreaHeight() {
        return this.FOCUS_AREA_HEIGHT;
    }

    public int getDefaultFocusAreaWidth() {
        return this.FOCUS_AREA_WIDTH;
    }

    public List<Area> getFocusArea(int i, int i2, int i3, int i4) {
        if (!this.mInitialized) {
            return null;
        }
        List<Area> arrayList = new ArrayList();
        arrayList.add(new Area(new Rect(), 1));
        calculateTapArea(i3, i4, 1.0f, i, i2, this.mPreviewWidth, this.mPreviewHeight, ((Area) arrayList.get(0)).rect);
        return arrayList;
    }

    public List<Area> getMeteringsArea(int i, int i2, int i3, int i4) {
        if (!this.mInitialized) {
            return null;
        }
        List<Area> arrayList = new ArrayList();
        arrayList.add(new Area(new Rect(), 1));
        calculateTapArea(i3, i4, 1.8f, i, i2, this.mPreviewWidth, this.mPreviewHeight, ((Area) arrayList.get(0)).rect);
        return arrayList;
    }

    public boolean isFocusingSnapOnFinish() {
        return this.mState == 2;
    }

    public boolean isInValidFocus() {
        return this.mState == 0 || this.mState == 4;
    }

    public boolean isNeedCancelAutoFocus() {
        return this.mCancelAutoFocusIfMove;
    }

    public void onAutoFocus(boolean z) {
        this.mState = z ? 3 : 4;
        this.mCancelAutoFocusIfMove = true;
    }

    public void onDeviceKeepMoving() {
        if (this.mState == 3 || this.mState == 4) {
            this.mState = 0;
        }
    }

    public void resetFocused() {
        this.mState = 0;
    }

    public void setPreviewSize(int i, int i2) {
        if (this.mPreviewWidth != i || this.mPreviewHeight != i2) {
            this.mPreviewWidth = i;
            this.mPreviewHeight = i2;
            setMatrix();
        }
    }
}
